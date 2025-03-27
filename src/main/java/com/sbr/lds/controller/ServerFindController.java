package com.sbr.lds.controller;

import com.sbr.common.model.H3Detail;
import com.sbr.common.model.ServerDetails;
import com.sbr.common.model.ServerDetailsWithDistance;
import com.sbr.lds.service.ServerFindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketFrameTypeMessageCondition;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
@MessageMapping("server")
public class ServerFindController {
    
    @Value("${location.threshold.distance}")
    private Double thresholdRadius;
    @Autowired
    private ServerFindService serverFindService;
    private static final String RSOCKET_FRAME_TYPE = RSocketFrameTypeMessageCondition.FRAME_TYPE_HEADER;
    private static final String CONTENT_TYPE = "contentType";
    
    @MessageMapping("all.find")
    public Flux<ServerDetails> serverDetails() {
        return this.serverFindService.serverDetails();
    }
    
    @MessageMapping("optimal.find")
    public Flux<ServerDetailsWithDistance> optimalServerDetails(Mono<H3Detail> h3DetailMono,@Header(required = false) String thresholdRadius) {
        return this.serverFindService.optimalServerDetails(h3DetailMono, Optional.ofNullable(thresholdRadius).map(Double::parseDouble).orElse(this.thresholdRadius));
    }
    
    @ConnectMapping("registration")
    public Mono<Void> requestsRegistration(RSocketRequester rSocketRequester,
                                           @Payload String clientId,
                                           @Header(RSOCKET_FRAME_TYPE) String rsocketFrameType,
                                           @Header(CONTENT_TYPE) String contentType) {
        return Mono.empty();
    }
}
