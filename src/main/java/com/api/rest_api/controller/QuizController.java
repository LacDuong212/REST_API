package com.api.rest_api.controller;

import com.api.rest_api.dto.QuizEditorRequest;
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

    @GetMapping("/{qid}/edit")
    public ResponseEntity<?> getQuizEditorByQid(@PathVariable Long qid, @RequestParam Long uid) {
        return quizService.getQuizEditorByQid(qid, uid);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizEditorRequest quizEditorRequest) {
        return quizService.createQuiz(quizEditorRequest);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateQuiz(@RequestBody QuizEditorRequest quizEditorRequest) {
        return quizService.updateQuiz(quizEditorRequest);
    }
}
