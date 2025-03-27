package com.sbr.lds.controller;

import com.sbr.common.model.Distance;
import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.sbr.common.model.Pair;
import com.sbr.lds.distance.Haversine;
import com.sbr.lds.h3.H3Reactive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketFrameTypeMessageCondition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Controller
@MessageMapping("h3")
public class H3RSController {
    
    @Value("${location.ranges.latrange}")
    private String latRange;
    @Value("${location.ranges.longrange}")
    private String longRange;
    @Value("${h3.default.resolution}")
    private Integer h3Resolution;
    @Autowired
    private H3Reactive h3;
    private static final String RSOCKET_FRAME_TYPE = RSocketFrameTypeMessageCondition.FRAME_TYPE_HEADER;
    private static final String CONTENT_TYPE = "contentType";
    
    @MessageMapping("distance")
    public Mono<Distance> distance() {
        String[] lats = Arrays.stream(this.latRange.trim().split("::")).map(String::trim).toArray(String[]::new);
        String[] longs = Arrays.stream(this.longRange.trim().split("::")).map(String::trim).toArray(String[]::new);
        LatLng start = LatLng.instance(lats[0], longs[0]);
        LatLng end = LatLng.instance(lats[1], longs[1]);
        return Mono.just(Distance.instance(String.valueOf(Haversine.distance(start, end)), start, end, null, null));
    }
    
    @MessageMapping("index")
    public Mono<String> latLngToH3Index(Mono<H3Detail> h3DetailMono) {
        return this.h3.latLngToH3Index(h3DetailMono, this.h3Resolution);
    }
    
    @MessageMapping("detail.{h3Index}")
    public Mono<H3Detail> h3IndexToH3Detail(@DestinationVariable String h3Index) {
        return this.h3.h3IndexToH3Detail(Mono.just(h3Index));
    }
    
    @MessageMapping("neighbours.map")
    public Flux<Pair<Integer, List<LatLng>>> neighboursMap(Mono<H3Detail> h3DetailMono) {
        return this.h3.neighboursMap(h3DetailMono, this.h3Resolution);
    }
    
    @PostMapping("neighbours.distance.map")
    public Flux<Pair<Integer, List<Distance>>> neighboursDistanceMap(Mono<H3Detail> h3DetailMono) {
        return this.h3.neighboursDistanceMap(h3DetailMono, this.h3Resolution);
    }
    
    @MessageMapping("neighbours")
    public Flux<LatLng> neighbours(Mono<H3Detail> h3DetailMono) {
        return this.h3.neighboursList(h3DetailMono, this.h3Resolution);
    }
    
    @MessageMapping("neighbours.distance")
    public Flux<Distance> neighboursDistance(Mono<H3Detail> h3DetailMono) {
        return this.h3.neighboursDistanceList(h3DetailMono, this.h3Resolution);
    }
    
    @MessageMapping("neighbours.distance.sort")
    public Flux<Distance> neighboursDistanceWithSort(Mono<H3Detail> h3DetailMono) {
        return this.h3.neighboursDistanceListWithSort(h3DetailMono, this.h3Resolution);
    }
    
    @ConnectMapping("registration")
    public Mono<Void> requestsRegistration(RSocketRequester rSocketRequester,
                                           @Payload String clientId,
                                           @Header(RSOCKET_FRAME_TYPE) String rsocketFrameType,
                                           @Header(CONTENT_TYPE) String contentType) {
        return Mono.empty();
    }
}
