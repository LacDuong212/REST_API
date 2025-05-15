package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "lobby")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lid;

    @Column(unique = true, nullable = false)
    private String code; // Mã phòng duy nhất

    @ManyToOne
    @JoinColumn(name = "qid", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String status; // PENDING, STARTED, FINISHED

    @Column
    private LocalDateTime startTime;
    
    @Column
    private Integer currentQuestionIndex;

    @OneToMany(mappedBy = "lobby")
    private List<LobbyParticipant> participants;
}
