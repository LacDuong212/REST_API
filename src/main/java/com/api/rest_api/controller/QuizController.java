package com.api.rest_api.controller;

import com.api.rest_api.dto.QuizRequest;
import com.api.rest_api.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
    @Autowired
    private QuizService quizService;

//    @GetMapping("/topic/{topicId}")
//    public ResponseEntity<?> getQuizzesByTopic(@PathVariable Long topicId) {
//        return quizService.getQuizzesByTopicId(topicId);
//    }

    @GetMapping("/top10")
    public ResponseEntity<?> getTop10Quizzes() {
        return quizService.getTop10QuizzesByAttempts();
    }

    @GetMapping("/created-past-7days")
    public ResponseEntity<?> getQuizzesCreatedPastWeek() {
        return quizService.getQuizzesCreatedPastWeek();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllQuizzes() {
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizRequest quizRequest) {
        return quizService.saveQuiz(quizRequest);
    }
}
