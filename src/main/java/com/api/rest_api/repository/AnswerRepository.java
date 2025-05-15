package com.api.rest_api.repository;

import com.api.rest_api.model.Answer;
import com.api.rest_api.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestion(Question question);
    boolean existsByAid(Long aid);

    Answer findByAid(Long aid);
}
