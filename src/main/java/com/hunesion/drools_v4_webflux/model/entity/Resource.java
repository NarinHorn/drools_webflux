package com.hunesion.drools_v4_webflux.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("resources")
public class Resource {
    @Id
    private Long id;
    private String resourceName;
    private String resourceType;
    private String description;
    private LocalDateTime createdAt;
}
