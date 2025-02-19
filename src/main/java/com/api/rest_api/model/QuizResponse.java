package com.api.rest_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "QuizResponses")
public class QuizResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid;

    private String answer;  // lưu câu trả lời cho một trong những loại câu hỏi: "MCQ", "True/False", "Short answer"; null = no answer

    @ManyToOne
    @JoinColumn(name = "qtid", nullable = false)
    private Question question;
    @ManyToOne
    @JoinColumn(name = "atid", nullable = false)
    private Attempt attempt;
}
