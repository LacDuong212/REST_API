package com.api.rest_api.service;

import com.api.rest_api.model.Quiz;
import com.api.rest_api.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;

    public ResponseEntity<?> getQuizzesByTopicId(Long topicId) {
        List<Quiz> quizzes = quizRepository.findByTopicId(topicId);
        return quizzes.isEmpty() ? ResponseEntity.ok("Không có quiz nào cho topic này") : ResponseEntity.ok(quizzes);
    }

    public ResponseEntity<?> getTop10QuizzesByAttempts() {
        List<Quiz> quizzesTop10 = quizRepository.findTop10ByMostAttempts(PageRequest.of(0, 10));
        return quizzesTop10.isEmpty() ? ResponseEntity.ok("Không có quiz nào phổ biến") : ResponseEntity.ok(quizzesTop10);
    }

}
