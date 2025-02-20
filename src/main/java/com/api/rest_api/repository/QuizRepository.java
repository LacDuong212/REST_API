package com.api.rest_api.repository;

import com.api.rest_api.model.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByTopic_Tid(Long topicId);

    @Query("SELECT q FROM Quiz q JOIN Attempt a ON q.qid = a.quiz.qid GROUP BY q.qid ORDER BY COUNT(a.atid) DESC")
    List<Quiz> findTop10ByMostAttempts(Pageable pageable);
    @Query("SELECT q FROM Quiz q WHERE q.createdDate >= CURRENT_DATE - 7")
    List<Quiz> findQuizzesCreatedPastWeek(Pageable pageable);
}
