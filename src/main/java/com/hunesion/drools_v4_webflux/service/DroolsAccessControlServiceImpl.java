package com.hunesion.drools_v4_webflux.service;

import com.hunesion.drools_v4_webflux.model.entity.*;
import com.hunesion.drools_v4_webflux.model.request.AccessRequest;
import com.hunesion.drools_v4_webflux.model.response.AccessResponse;
import com.hunesion.drools_v4_webflux.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

@Service
public class DroolsAccessControlServiceImpl implements DroolsAccessControlService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private final KieServices kieServices = KieServices.Factory.get();
    private KieContainer kieContainer;

    public DroolsAccessControlServiceImpl() {
        kieContainer = kieServices.getKieClasspathContainer();
    }

    @Override
    public Mono<AccessResponse> checkAccess(AccessRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .flatMap(user ->
                        userRoleRepository.findRoleIdsByUserId(user.getId())
                                .collectList()
                                .flatMap(roleIds ->
                                        roleRepository.findAllById(roleIds)
                                                .collectList()
                                                .flatMap(roles ->
                                                        resourceRepository.findByResourceName(request.getResourceName())
                                                                .flatMap(resource ->
                                                                        actionRepository.findByActionName(request.getActionName())
                                                                                .flatMap(action ->
                                                                                        evaluateAccessWithDrools(user, roles, resource, action)
                                                                                )
                                                                )
                                                )
                                )
                )
                .switchIfEmpty(Mono.just(AccessResponse.deny(
                        request.getUsername(),
                        request.getResourceName(),
                        request.getActionName()
                )));
    }

    private Mono<AccessResponse> evaluateAccessWithDrools(
            User user,
            List<Role> roles,
            Resource resource,
            Action action) {

        return policyRepository.findByRoleIdAndEnabledTrueOrderByPriorityDesc(roles.get(0).getId())
                .collectList()
                .map(policies -> {
                    // Create Drools session
                    KieSession kieSession = kieContainer.newKieSession("accessControlSession");

                    // Create access decision object
                    AccessDecision decision = new AccessDecision();
                    decision.setUser(user);
                    decision.setRoles(roles);
                    decision.setResource(resource);
                    decision.setAction(action);
                    decision.setPolicies(policies);
                    decision.setAllowed(false); // Default deny

                    // Insert facts into Drools
                    kieSession.insert(decision);
                    kieSession.insert(user);
                    roles.forEach(kieSession::insert);
                    kieSession.insert(resource);
                    kieSession.insert(action);
                    policies.forEach(kieSession::insert);

                    // Fire rules
                    kieSession.fireAllRules();
                    kieSession.dispose();

                    // Return response
                    if (decision.isAllowed()) {
                        return AccessResponse.allow(
                                user.getUsername(),
                                resource.getResourceName(),
                                action.getActionName()
                        );
                    } else {
                        return AccessResponse.deny(
                                user.getUsername(),
                                resource.getResourceName(),
                                action.getActionName()
                        );
                    }
                });
    }

    // Helper class for Drools decision
    public static class AccessDecision {
        private User user;
        private List<Role> roles = new ArrayList<>();
        private Resource resource;
        private Action action;
        private List<Policy> policies = new ArrayList<>();
        private boolean allowed;

        // Getters and setters
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }

        public List<Role> getRoles() { return roles; }
        public void setRoles(List<Role> roles) { this.roles = roles; }

        public Resource getResource() { return resource; }
        public void setResource(Resource resource) { this.resource = resource; }

        public Action getAction() { return action; }
        public void setAction(Action action) { this.action = action; }

        public List<Policy> getPolicies() { return policies; }
        public void setPolicies(List<Policy> policies) { this.policies = policies; }

        public boolean isAllowed() { return allowed; }
        public void setAllowed(boolean allowed) { this.allowed = allowed; }
    }
}
