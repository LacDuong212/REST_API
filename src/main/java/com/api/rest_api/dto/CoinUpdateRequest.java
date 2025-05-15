package com.api.rest_api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinUpdateRequest {
    private Integer coins; // Maps to CoinHistory.amount
}
