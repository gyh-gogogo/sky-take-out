package com.sky.controller.ai;

import com.sky.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "AI聊天相关接口")
@RequestMapping("/ai/chat")
@Slf4j

public class AiChatController {
    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    @ApiOperation("阻塞式AI聊天问答")
    public String ask(String question) {
        log.info("Received question: {}", question);
        // 这里可以调用AI服务进行处理
        return aiService.chat(question);
    }

}
