package com.hunesion.drools_v4_webflux.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("roles")
public class Role {
    @Id
    private Long id;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
}
