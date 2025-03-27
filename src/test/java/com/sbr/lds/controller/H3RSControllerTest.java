package com.sbr.lds.controller;

import com.sbr.common.model.Distance;
import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.sbr.common.model.Pair;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class H3RSControllerTest {
    
    @Autowired
    private RSocketRequester.Builder rSocketRequesterBuilder;
    
    private RSocketRequester rSocketRequester;
    
    @BeforeAll
    public void setup() {
        this.rSocketRequester = this.rSocketRequesterBuilder.transport(TcpClientTransport.create("localhost", 9085));
    }
    
    @Test
    public void testLatLngToH3Index() {
        Mono<String> h3IndexMono = this.rSocketRequester.route("h3.index")
                                              .data(H3Detail.instance(LatLng.instance("13.488109844395439", "77.61887581848227"), 9, null))
                                              .retrieveMono(String.class).doOnNext(System.out::println);
        StepVerifier.create(h3IndexMono).expectNextCount(1).verifyComplete();
    }
    
    @Test
    public void testH3IndexToH3Detail() {
        Mono<H3Detail> h3DetailMono = this.rSocketRequester.route("h3.detail.8960168821bffff")
                                              .retrieveMono(H3Detail.class).doOnNext(System.out::println);
        StepVerifier.create(h3DetailMono).expectNextCount(1).verifyComplete();
    }
    
    @Test
    public void testFetchNeighboursMap() {
        Flux<Pair<Integer, List<LatLng>>> latLngFlux = this.rSocketRequester.route("h3.neighbours.map")
                                                               .data(H3Detail.instance(LatLng.instance("13.486406", "77.619198"), 9, 10))
                                                               .retrieveFlux(new ParameterizedTypeReference<Pair<Integer, List<LatLng>>>() {}).doOnNext(System.out::println);
        StepVerifier.create(latLngFlux).expectNextCount(11).verifyComplete();
    }
    
    @Test
    public void testFetchNeighboursDistanceWithSort() {
        Flux<Distance> distanceFlux = this.rSocketRequester.route("h3.neighbours.distance.sort")
                                                               .data(H3Detail.instance(LatLng.instance("13.486406", "77.619198"), 9, 10))
                                                               .retrieveFlux(Distance.class).doOnNext(System.out::println)
                                                               .take(20);
        StepVerifier.create(distanceFlux).expectNextCount(20).verifyComplete();
    }
}
