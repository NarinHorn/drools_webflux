package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.Action;
import com.hunesion.drools_v4_webflux.model.entity.Policy;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ActionRepository extends ReactiveCrudRepository<Action, Long> {
    @Query("SELECT * FROM actions WHERE action_name = :actionName")
    Mono<Action> findByActionName(String actionName);
}
