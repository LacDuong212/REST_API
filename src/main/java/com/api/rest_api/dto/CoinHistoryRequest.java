package com.api.rest_api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinHistoryRequest {
    private Long uid;
    private Integer coins; // Maps to amount
    private String description;
    private String transactionTime; // ISO 8601, convert to LocalDateTime
}
