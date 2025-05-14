package com.api.rest_api.repository;

import com.api.rest_api.model.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    //List<Quiz> findByTopic_Tid(Long topicId);

    @Query("SELECT q FROM Quiz q JOIN Attempt a ON q.qid = a.quiz.qid GROUP BY q.qid ORDER BY COUNT(a.atid) DESC")
    List<Quiz> findTop10ByMostAttempts(Pageable pageable);
    @Query("SELECT q FROM Quiz q WHERE q.createdDate >= :oneWeekAgo")
    List<Quiz> findQuizzesCreatedPastWeek(@Param("oneWeekAgo") LocalDate oneWeekAgo, Pageable pageable);
}
