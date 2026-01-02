package com.hunesion.drools_v4_webflux.model.request;

import lombok.Data;

@Data
public class AccessRequest {
    private String username;
    private String resourceName; // e.g., "SSH", "rm", "ls"
    private String actionName;   // e.g., "EXECUTE"
    private String resourceType; // e.g., "COMMAND", "SERVICE"
}
