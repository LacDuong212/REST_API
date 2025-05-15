package com.api.rest_api.controller;

import com.api.rest_api.dto.QuizAnswerRequest;
import com.api.rest_api.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private QuizService quizService;

    @MessageMapping("/submitAnswer")
    public void submitAnswer(QuizAnswerRequest request) {
        logger.info("Received answer submission via STOMP: lid={}, uid={}, qtid={}, aid={}", 
                   request.getLid(), request.getUid(), request.getQtid(), request.getAid());
        try {
            quizService.submitAnswer(request);
            logger.info("Answer processed successfully");
        } catch (Exception e) {
            logger.error("Error processing answer: {}", e.getMessage(), e);
        }
    }
}
