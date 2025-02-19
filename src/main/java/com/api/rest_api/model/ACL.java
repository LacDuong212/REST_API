package com.api.rest_api.model;

import com.api.rest_api.enums.AclPermission;
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
    AclPermission permission;
}
