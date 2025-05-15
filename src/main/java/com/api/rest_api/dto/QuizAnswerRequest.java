package com.api.rest_api.dto;

import lombok.Data;

@Data
public class QuizAnswerRequest {
    private Long lid;
    private Long uid;
    private Long qtid;
    private Long aid;
}
