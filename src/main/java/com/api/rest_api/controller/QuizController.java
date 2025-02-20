package com.api.rest_api.controller;

import com.api.rest_api.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    @Autowired
    private QuizService quizService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getQuizzesByTopic(@PathVariable Long topicId) {
        return quizService.getQuizzesByTopicId(topicId);
    }

    @GetMapping("/top10")
    public ResponseEntity<?> getTop10Quizzes() {
        return quizService.getTop10QuizzesByAttempts();
    }

    @GetMapping("/created-past-7days")
    public ResponseEntity<?> getQuizzesCreatedPastWeek() {
        return quizService.getQuizzesCreatedPastWeek();
    }
}
