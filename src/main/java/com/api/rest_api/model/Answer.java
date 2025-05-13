package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(nullable = false, length = 500)
    private String text;
    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "qtid", nullable = false)
    private Question question;
}
