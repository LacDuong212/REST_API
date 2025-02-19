package com.api.rest_api.model;

import com.api.rest_api.enums.AclRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "acl")
public class ACL {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aclid;

    @Enumerated(EnumType.STRING)
    AclRole role;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private Account account;
    @ManyToOne
    @JoinColumn(name = "qid", nullable = false)
    private Quiz quiz;
}
