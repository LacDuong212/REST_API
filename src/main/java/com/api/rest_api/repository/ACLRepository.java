package com.api.rest_api.repository;

import com.api.rest_api.model.ACL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ACLRepository extends JpaRepository<ACL, Long> {
    boolean existsByAclid(Long aclid);
    boolean existsByAccount_UidAndQuiz_Qid(Long uid, Long qid);

    ACL findByAclid(Long aclid);
}
