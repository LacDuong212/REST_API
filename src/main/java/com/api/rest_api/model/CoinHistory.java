package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "coin_history")
public class CoinHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hid;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String description;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private Account account;
}
