package com.api.rest_api.controller;

import com.api.rest_api.dto.JoinLobbyRequest;
import com.api.rest_api.dto.LobbyRequest;
import com.api.rest_api.dto.LobbyResponse;
import com.api.rest_api.dto.StartLobbyRequest;
import com.api.rest_api.service.QuizService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {
    @Autowired
    private QuizService quizService;

    @PostMapping("/create")
    public ResponseEntity<LobbyResponse> createLobby(@RequestBody LobbyRequest request) {
        return ResponseEntity.ok(quizService.createLobby(request));
    }

    @PostMapping("/join")
    public ResponseEntity<Void> joinLobby(@RequestBody JoinLobbyRequest request) {
        quizService.joinLobby(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{lid}")
    public ResponseEntity<LobbyResponse> getLobbyInfo(@PathVariable Long lid) {
        LobbyResponse response = quizService.getLobbyInfo(lid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{lid}/start")
    public ResponseEntity<Void> startLobby(@PathVariable Long lid, @RequestBody StartLobbyRequest request) {
        quizService.startLobby(lid, request.getUid());
        return ResponseEntity.ok().build();
    }
}
