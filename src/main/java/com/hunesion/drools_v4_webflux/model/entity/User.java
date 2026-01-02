package com.hunesion.drools_v4_webflux.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String department;
    private LocalDateTime createdAt;
}
