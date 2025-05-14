package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quizs")
public class Quiz {
    @Id
    private Long qid;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private LocalDate createdDate;

    private long duration;
    private String topic;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ACL> aclRoles = new ArrayList<>();
//    @ManyToOne
//    @JoinColumn(name = "tid", nullable = false)
//    private Topic topic;
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attempt> attempts = new ArrayList<>();
}
