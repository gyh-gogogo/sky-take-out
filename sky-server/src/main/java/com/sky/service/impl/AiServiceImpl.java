package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.AiService;
import io.github.imfangs.dify.client.DifyChatClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.callback.ChatStreamCallback;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.event.ErrorEvent;
import io.github.imfangs.dify.client.event.MessageEndEvent;
import io.github.imfangs.dify.client.event.MessageEvent;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service

public class AiServiceImpl implements AiService {

    @Autowired
    ThreadPoolExecutor ioThreadPool;


    @Override
    public String chat(String question) {

        Long userId = BaseContext.getCurrentId();

        // 检测当前question是否为空
        if (question == null || question.trim().isEmpty()) {
            return Result.error("问题不能为空").toString();
        }
        // 这里可以集成AI模型进行问答处理
        DifyChatClient difyChatClient = initDify();
        ChatMessage message = getChatMessage(question, userId, ResponseMode.BLOCKING);

        String response;
        try {
            ChatMessageResponse chatMessageResponse = difyChatClient.sendChatMessage(message);
            response = chatMessageResponse.getAnswer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭客户端连接
            difyChatClient.close();
        }
        return response;
    }

    @Override
    public SseEmitter chatStream(String question) {
        SseEmitter sseEmitter = new SseEmitter(30000L); // 30秒超时
        Long userId = BaseContext.getCurrentId();

        // 添加状态标志
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        DifyChatClient difyChatClient = initDify();
        ChatMessage message = getChatMessage(question, userId, ResponseMode.STREAMING);

        // 设置超时和错误处理
        sseEmitter.onTimeout(() -> {
            log.warn("SSE连接超时，用户ID: {}", userId);
            isCompleted.set(true);
        });

        sseEmitter.onError((throwable) -> {
            log.warn("SSE连接出错，用户ID: {}, 错误: {}", userId, throwable.getMessage());
            isCompleted.set(true);
        });

        sseEmitter.onCompletion(() -> {
            log.info("SSE连接完成，用户ID: {}", userId);
            isCompleted.set(true);
        });

        ioThreadPool.execute(() -> {
            try {
                difyChatClient.sendChatMessageStream(message, new ChatStreamCallback() {
                    @Override
                    public void onError(ErrorEvent event) {
                        log.error("Dify流处理出错: {}", event.getMessage());
                        if (!isCompleted.get()) {
                            sseEmitter.completeWithError(new IOException(event.getMessage()));
                            isCompleted.set(true);
                        }
                    }

                    @Override
                    public void onMessage(MessageEvent event) {
                        String answer = event.getAnswer();
                        if (answer != null && !isCompleted.get()) {
                            try {
                                sseEmitter.send(answer);
                            } catch (IllegalStateException e) {
                                // SseEmitter 已经完成，停止发送
                                log.warn("SseEmitter已完成，停止发送数据: {}", e.getMessage());
                                isCompleted.set(true);
                            } catch (IOException e) {
                                log.error("发送SSE数据失败: {}", e.getMessage());
                                if (!isCompleted.get()) {
                                    sseEmitter.completeWithError(e);
                                    isCompleted.set(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onMessageEnd(MessageEndEvent event) {
                        if (!isCompleted.get()) {
                            sseEmitter.complete();
                            isCompleted.set(true);
                        }
                    }

                });
            } catch (Exception e) {
                log.error("启动Dify流失败: {}", e.getMessage());
                if (!isCompleted.get()) {
                    sseEmitter.completeWithError(e);
                    isCompleted.set(true);
                }
            }
        });

        return sseEmitter;
    }


    private static ChatMessage getChatMessage(String question, Long currentId, ResponseMode responseMode) {
        return ChatMessage.builder()
                .query(question)
                .user("1L")
                .responseMode(responseMode)
                .build();
    }

    private DifyChatClient initDify() {
        return DifyClientFactory.createChatClient("http://192.168.150.107/v1", "app-vKU1C1YH373psmd1p5sGhfpd");
    }
}
