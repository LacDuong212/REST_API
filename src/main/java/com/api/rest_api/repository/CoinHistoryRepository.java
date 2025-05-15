package com.api.rest_api.repository;

import com.api.rest_api.model.CoinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinHistoryRepository extends JpaRepository<CoinHistory, Long> {
    @Query("SELECT COALESCE(SUM(ch.amount), 0) FROM CoinHistory ch WHERE ch.account.uid = :uid")
    Integer sumAmountByAccountUid(Long uid);

    List<CoinHistory> findByAccountUid(Long uid);
}
