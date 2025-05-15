package com.api.rest_api.repository;

import com.api.rest_api.model.Question;
import com.api.rest_api.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz(Quiz quiz);
    
    int countByQuiz(Quiz quiz);
    
    @Query("SELECT q FROM Question q JOIN FETCH q.answers WHERE q.qtid = ?1")
    Question findByIdWithAnswers(Long qtid);
}
