package com.api.rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizRequest {
    private long qid;
    private String title = "";
    private String description = "";
    private String topic = "";   // category
    private boolean isPublic;
    private String createdDate;
    private int questionCount = 0;
    private long duration;   // in seconds
    private long uid = -1;
    private int attemptCount = 0;
    private int questionType = 0;
}
