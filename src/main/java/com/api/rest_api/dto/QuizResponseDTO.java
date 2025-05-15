package com.api.rest_api.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponseDTO {
    private Long qid;
    private String title;
    private long duration;
    private List<QuestionResponse> questions;
}
