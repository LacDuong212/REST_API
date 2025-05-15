package com.api.rest_api.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long qtid;
    private String question;
    private String type;
    private List<AnswerResponse> answers;
}
