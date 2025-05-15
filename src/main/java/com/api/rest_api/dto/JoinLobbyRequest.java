package com.api.rest_api.dto;

import lombok.Data;

@Data
public class JoinLobbyRequest {
    private String code;
    private Long uid;
}
