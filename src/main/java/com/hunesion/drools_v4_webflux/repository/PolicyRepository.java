package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.Policy;
import com.hunesion.drools_v4_webflux.model.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyRepository extends ReactiveCrudRepository<Policy, Long> {
    @Query("SELECT * FROM policies WHERE role_id = :roleId AND enabled = true ORDER BY priority DESC")
    Flux<Policy> findByRoleIdAndEnabledTrueOrderByPriorityDesc(Long roleId);

    @Query("SELECT * FROM policies WHERE role_id = :roleId AND resource_id = :resourceId AND action_id = :actionId AND enabled = true ORDER BY priority DESC")
    Flux<Policy> findByRoleAndResourceAndAction(Long roleId, Long resourceId, Long actionId);
}
