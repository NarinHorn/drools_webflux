package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.Role;
import com.hunesion.drools_v4_webflux.model.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    @Query("SELECT * FROM roles WHERE role_name = :roleName")
    Mono<Role> findByRoleName(String roleName);
}
