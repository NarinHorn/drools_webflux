package com.hunesion.drools_v4_webflux.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("user_roles")
public class UserRole {
    @Id
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime assignedAt;
}
