package com.hunesion.drools_v4_webflux.controller;

import com.hunesion.drools_v4_webflux.model.request.AccessRequest;
import com.hunesion.drools_v4_webflux.model.response.AccessResponse;
import com.hunesion.drools_v4_webflux.service.DroolsAccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/access")
public class AccessControlController {

    @Autowired
    private DroolsAccessControlService accessControlService;

    /**
     * Check if user can access a resource
     * Example: POST /api/access/check
     * Body: {
     *   "username": "john_developer",
     *   "resourceName": "rm",
     *   "actionName": "EXECUTE",
     *   "resourceType": "COMMAND"
     * }
     */
    @PostMapping("/check")
    public Mono<AccessResponse> checkAccess(@RequestBody AccessRequest request) {
        return accessControlService.checkAccess(request);
    }

    /**
     * Example endpoint for SSH access check
     * POST /api/access/ssh
     */
    @PostMapping("/ssh")
    public Mono<AccessResponse> checkSSHAccess(@RequestParam String username) {
        AccessRequest request = new AccessRequest();
        request.setUsername(username);
        request.setResourceName("SSH");
        request.setActionName("EXECUTE");
        request.setResourceType("SERVICE");
        return accessControlService.checkAccess(request);
    }

    /**
     * Example endpoint for command access check
     * POST /api/access/command?username=john_developer&command=rm
     */
    @PostMapping("/command")
    public Mono<AccessResponse> checkCommandAccess(
            @RequestParam String username,
            @RequestParam String command) {
        AccessRequest request = new AccessRequest();
        request.setUsername(username);
        request.setResourceName(command);
        request.setActionName("EXECUTE");
        request.setResourceType("COMMAND");
        return accessControlService.checkAccess(request);
    }
}
