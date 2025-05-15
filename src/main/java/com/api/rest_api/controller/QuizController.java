package com.api.rest_api.controller;

import com.api.rest_api.dto.*;
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

    @GetMapping("/{qid}")
    public ResponseEntity<QuizResponseDTO> getQuizById(@PathVariable Long qid) {
        return ResponseEntity.ok(quizService.getQuizById(qid));
    }

    @PostMapping("/attempts")
    public ResponseEntity<AttemptResponse> createAttempt(@RequestBody AttemptRequest request) {
        return ResponseEntity.ok(quizService.createAttempt(request));
    }

    @PostMapping("/quizResponses")
    public ResponseEntity<Void> createQuizResponse(@RequestBody QuizResponseRequest request) {
        quizService.createQuizResponse(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/coinHistory")
    public ResponseEntity<Void> createCoinHistory(@RequestBody CoinHistoryRequest request) {
        quizService.createCoinHistory(request);
        return ResponseEntity.ok().build();
    }

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

    @GetMapping("/{uid}/quizzes")
    public ResponseEntity<?> getQuizzesCreatedByAccount(@PathVariable Long uid) {
        return quizService.getQuizzesCreatedByUid(uid);
    }

    @GetMapping("/{uid}/attempted")
    public ResponseEntity<?> getQuizzesAttemptedByAccount(@PathVariable Long uid) {
        return quizService.getQuizzesAttemptedByUid(uid);
    }

    @GetMapping("/{topic}/public")
    public ResponseEntity<?> getPublicQuizzesByTopic(@PathVariable String topic) {
        return quizService.getPublicQuizzesByTopic(topic);
    }

    @GetMapping("/public/{keyword}")
    public ResponseEntity<?> getPublicQuizzesByKeyword(@PathVariable String keyword) {
        return quizService.getPublicQuizzesByKeyword(keyword);
    }
}
