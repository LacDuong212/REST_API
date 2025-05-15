package com.api.rest_api.dto;

import com.api.rest_api.model.Answer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerModel {
    private long aid;
    private String text = "";
    private boolean correct;
    private long qtid;

    public AnswerModel(Answer answer) {
        this.aid = answer.getAid();
        this.text = answer.getText();
        this.correct = answer.isCorrect();
        this.qtid = answer.getQuestion().getQtid();
    }
}
