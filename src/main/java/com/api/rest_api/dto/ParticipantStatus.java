package com.api.rest_api.dto;

import lombok.Data;

@Data
public class ParticipantStatus {
    private Long uid;
    private String username;
    private Integer score;
}
