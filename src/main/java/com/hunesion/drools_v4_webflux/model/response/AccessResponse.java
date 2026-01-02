package com.hunesion.drools_v4_webflux.model.response;

import lombok.Data;

@Data
public class AccessResponse {
    private boolean allowed;
    private String message;
    private String username;
    private String resourceName;
    private String actionName;

    public static AccessResponse allow(String username, String resource, String action) {
        AccessResponse response = new AccessResponse();
        response.allowed = true;
        response.message = "ALLOWED";
        response.username = username;
        response.resourceName = resource;
        response.actionName = action;
        return response;
    }

    public static AccessResponse deny(String username, String resource, String action) {
        AccessResponse response = new AccessResponse();
        response.allowed = false;
        response.message = "NOT ALLOWED";
        response.username = username;
        response.resourceName = resource;
        response.actionName = action;
        return response;
    }
}
