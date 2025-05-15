package com.api.rest_api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponseRequest {
    private Long atid;
    private Long qtid;
    private String answer; // aid for MCQ, text for others
}
