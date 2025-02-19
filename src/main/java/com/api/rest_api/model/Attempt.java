package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attempts")
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long atid;

    @Column(nullable = false)
    private Float score;

    @Column(nullable = false)
    private Timestamp submitTime;

    @Column(nullable = false)
    private Integer attemptTime;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = true)
    private Account account;
    @ManyToOne
    @JoinColumn(name = "gid", nullable = true)
    private Guest guest;
    @ManyToOne
    @JoinColumn(name = "qid", nullable = false)
    private Quiz quiz;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizResponse> quizResponses = new ArrayList<>();
}
