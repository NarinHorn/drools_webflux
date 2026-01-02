package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @Query("SELECT * FROM users WHERE username = :username")
    Mono<User> findByUsername(String username);
}
