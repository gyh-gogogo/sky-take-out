package com.sky.service.impl;

import com.sky.result.Result;
import com.sky.service.AiService;
import io.github.imfangs.dify.client.DifyChatClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AiServiceImpl implements AiService {
    @Override
    public String chat(String question) {
        // TODO 你自己获得用户Id

        // 检测当前question是否为空
        if (question == null || question.trim().isEmpty()) {
            return Result.error("问题不能为空").toString();
        }
        // 这里可以集成AI模型进行问答处理
        DifyChatClient difyChatClient = initDify();
        ChatMessage message = getChatMessage(question, 1L, ResponseMode.BLOCKING);

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


    private static ChatMessage getChatMessage(String question, Long currentId, ResponseMode responseMode) {
        return ChatMessage.builder()
                .query(question)
                .user(currentId.toString())
                .responseMode(responseMode)
                .build();
    }

    private DifyChatClient initDify() {
        return DifyClientFactory.createChatClient("http://192.168.150.107/v1", "app-vKU1C1YH373psmd1p5sGhfpd");
    }
}
