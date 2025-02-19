package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
}
