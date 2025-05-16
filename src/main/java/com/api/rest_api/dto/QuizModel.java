package com.api.rest_api.dto;

import com.api.rest_api.model.Account;
import com.api.rest_api.model.Quiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizModel {
    private long qid = -1L;
    private String title = "";
    private String description = "";
    private String topic = "";   // category
    private boolean visible = true;
    private String createdDate;
    private int questionCount = 0;
    private long duration = 0;   // in seconds

    private long uid = -1L;
    private String username;
    private String pfp;

    private int attemptCount = 0;
    private int questionType = 0;   //1:mcq, 2:true/false, 3:short answer, 4: mcq + true/false, 5: mcq + short answer, 6: true/false + short answer, 7: all

    public String getDurationString() {
        int hours = (int) (duration / 3600);
        int minutes = (int) ((duration % 3600) / 60);
        int seconds = (int) (duration % 60);
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }

    public void setDurationFromString(String h, String m, String s) {
        if (h == null || h.isEmpty()) h = "0";
        if (m == null || m.isEmpty()) m = "0";
        if (s == null || s.isEmpty()) s = "0";
        this.duration = Long.parseLong(h) * 3600 + Long.parseLong(m) * 60 + Long.parseLong(s);
    }

    public QuizModel(Quiz quiz) {
        this.qid = quiz.getQid();
        this.title = quiz.getTitle();
        this.description = quiz.getDescription();
        this.topic = quiz.getTopic();
        this.visible = quiz.isPublic();
        this.createdDate = quiz.getCreatedDate().toString();
        this.questionCount = quiz.getQuestions().size();
        this.duration = quiz.getDuration();
        if (quiz.getAclRoles() != null && !quiz.getAclRoles().isEmpty()) {
            Account a = quiz.getAclRoles().getFirst().getAccount();
            this.uid = a.getUid();
            this.username = a.getUsername();
            this.pfp = a.getImage();
        }
        this.attemptCount = quiz.getAttempts().size();
    }
}