package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lobby_participant")
public class LobbyParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lid", nullable = false)
    private Lobby lobby;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private Account account;

    @Column
    private Integer score; // Điểm số trong lobby
}
