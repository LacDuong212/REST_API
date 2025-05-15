package com.api.rest_api.repository;

import com.api.rest_api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUid(Long uid);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Account findByUid(Long uid);
    Account findByEmail(String email);
    Account findByUsername(String username);
    Account findByEmailAndPassword(String email, String password);

    @Query("SELECT a FROM Account a WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<Account> searchAccountsByUsername(@Param("username") String username);
}
