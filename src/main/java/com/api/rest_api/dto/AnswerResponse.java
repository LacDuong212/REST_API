package com.api.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private Long aid;
    private String text;
    @JsonProperty("isCorrect")
    private Boolean isCorrect;
}
