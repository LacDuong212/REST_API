package com.api.rest_api.dto;

import lombok.Data;

import java.util.List;

@Data
public class LobbyStatus {
    private Long lid;
    private String status;
    private Long qid;
    private Integer currentQuestionIndex;
    private List<ParticipantStatus> participants;
}