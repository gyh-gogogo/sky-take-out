package com.sky.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiService {
    String chat(String question);


    SseEmitter chatStream(String question);
}
