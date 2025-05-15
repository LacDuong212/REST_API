package com.api.rest_api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttemptRequest {
    private Double score; // Cast to Float
    private String submitTime; // ISO 8601, convert to Timestamp
    private Integer attemptTime;
    private Long uid; // Nullable
    private Long qid;
}
