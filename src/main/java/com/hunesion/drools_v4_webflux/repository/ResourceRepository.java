package com.hunesion.drools_v4_webflux.repository;

import com.hunesion.drools_v4_webflux.model.entity.Resource;
import com.hunesion.drools_v4_webflux.model.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ResourceRepository extends ReactiveCrudRepository<Resource, Long> {
    @Query("SELECT * FROM resources WHERE resource_name = :resourceName")
    Mono<Resource> findByResourceName(String resourceName);
}
