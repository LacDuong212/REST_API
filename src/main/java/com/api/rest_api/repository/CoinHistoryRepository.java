package com.api.rest_api.repository;

import com.api.rest_api.model.CoinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CoinHistoryRepository extends JpaRepository<CoinHistory, Long> {
    @Query("SELECT COALESCE(SUM(ch.amount), 0) FROM CoinHistory ch WHERE ch.account.uid = :uid")
    Integer sumAmountByAccountUid(Long uid);

    List<CoinHistory> findByAccountUid(Long uid);
    
    @Query("SELECT a.uid, a.username, a.fullname, a.image, SUM(ch.amount) as totalCoins FROM CoinHistory ch " +
           "JOIN ch.account a " +
           "WHERE CAST(ch.timestamp AS date) >= CAST(:startDate AS date) " +
           "GROUP BY a.uid, a.username, a.fullname, a.image " +
           "ORDER BY totalCoins DESC")
    List<Object[]> findUserRankingsByPeriod(@Param("startDate") Date startDate);
    
    @Query("SELECT a.uid, a.username, a.fullname, a.image, SUM(ch.amount) as totalCoins FROM CoinHistory ch " +
           "JOIN ch.account a " +
           "GROUP BY a.uid, a.username, a.fullname, a.image " +
           "ORDER BY totalCoins DESC")
    List<Object[]> findAllTimeUserRankings();
}
