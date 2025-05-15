package com.api.rest_api.dto;

import com.api.rest_api.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionModel {
    private Long qtid;
    private String question = "";
    private String type = "MCQ";
    private List<AnswerModel> answers;

    public QuestionModel(Question question) {
        this.qtid = question.getQtid();
        this.question = question.getQuestion();
        this.type = question.getType();
        this.answers = question.getAnswers().stream().map(AnswerModel::new).toList();
    }
}
