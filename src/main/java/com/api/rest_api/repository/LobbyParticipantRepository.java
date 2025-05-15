package com.api.rest_api.repository;

import com.api.rest_api.model.LobbyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LobbyParticipantRepository extends JpaRepository<LobbyParticipant, Long> {
    List<LobbyParticipant> findByLobbyLid(Long lid);
}
