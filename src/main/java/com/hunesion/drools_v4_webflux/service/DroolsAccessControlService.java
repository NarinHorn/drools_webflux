package com.hunesion.drools_v4_webflux.service;

import com.hunesion.drools_v4_webflux.model.request.AccessRequest;
import com.hunesion.drools_v4_webflux.model.response.AccessResponse;
import reactor.core.publisher.Mono;

public interface DroolsAccessControlService {
    Mono<AccessResponse> checkAccess(AccessRequest request);
}
