package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.User;
import com.hunesion.drools_v4_webflux.model.entity.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {
    @Query("SELECT role_id FROM user_roles WHERE user_id = :userId")
    Flux<Long> findRoleIdsByUserId(Long userId);
}
