package com.sbr.lds.service;

import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.sbr.common.model.ServerDetailsWithDistance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerFindServiceTest {
    
    @Autowired
    private ServerFindService serverFindService;
    
    @Test
    public void testOptimalServerDetails() {
        Flux<ServerDetailsWithDistance> latLngFlux = this.serverFindService.optimalServerDetails(Mono.just(H3Detail.instance(LatLng.instance("13.486406", "77.619198"), 9, 10)), 1000.0)
                                                             .doOnNext(System.out::println);
        StepVerifier.create(latLngFlux).expectNextCount(6).verifyComplete();
    }
}
