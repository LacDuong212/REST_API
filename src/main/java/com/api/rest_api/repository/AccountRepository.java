package com.api.rest_api.repository;

import com.api.rest_api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Account findByUid(Long uid);
    Account findByEmail(String email);
    Account findByUsername(String username);
    Account findByEmailAndPassword(String email, String password);

}
