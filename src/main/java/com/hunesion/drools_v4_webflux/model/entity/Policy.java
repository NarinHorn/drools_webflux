package com.hunesion.drools_v4_webflux.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("policies")
public class Policy {
    @Id
    private Long id;
    private String policyName;
    private Long roleId;
    private Long resourceId;
    private Long actionId;
    private String effect; // ALLOW or DENY
    private String conditions; // JSON string
    private Integer priority;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
