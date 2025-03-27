package com.sbr.lds.service;

import com.sbr.common.model.*;
import com.sbr.lds.h3.H3Reactive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.IntStream;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ServerFindService {
    private final H3Reactive h3;
    
    @Value("${h3.default.resolution}")
    private Integer h3Resolution;
    
    // TODO: Now implementing this method with dummy data, in the future will update it with data in the storage( can be redis, nginx or mongo)
    public Flux<ServerDetails> serverDetails() {
        return Flux.create(sink -> {
            IntStream.range(1, 11)
                    .mapToObj(i -> ServerDetails.builder().id("Test" + i).location(LatLng.instance(String.valueOf(13.486406 + i),String.valueOf(77.619198 + i))).build())
                    .forEach(sink::next);
            sink.complete();
        });
    }
    
    public Flux<ServerDetailsWithDistance> optimalServerDetails(Mono<H3Detail> h3DetailMono, Double thresholdRadius) {
        List<ServerDetailsWithDistance> optimalServers = new ArrayList<>();
        return Flux.create(sink ->
            h3DetailMono.subscribe(h3Detail ->
                this.serverDetails()
                        .doOnNext(server ->
                            this.h3.distance(Mono.just(h3Detail.getLocation()), Mono.just(server.getLocation())).subscribe(distance -> {
                                if(distance <= thresholdRadius){
                                    this.h3.latLngToH3Index(h3DetailMono, this.h3Resolution).subscribe(h3StartIndex ->
                                        this.h3.latLngToH3Index(
                                                Mono.just(H3Detail.builder().location(server.getLocation()).resolution(h3Detail.getResolution()).build()),
                                                this.h3Resolution
                                        ).subscribe(h3EndIndex -> {
                                            Distance dist = Distance.instance(String.valueOf(distance), h3Detail.getLocation(), server.getLocation(), h3StartIndex, h3EndIndex);
                                            optimalServers.add(ServerDetailsWithDistance.instance(server, dist));
                                        }));
                                }
                            }))
                        .doOnComplete(() -> {
                            optimalServers.sort(Comparator.comparingDouble(d -> Double.parseDouble(d.getDistance().getDistance())));
                            optimalServers.forEach(sink::next);
                        })
                        .doFinally(signal -> sink.complete())
                        .subscribe()));
    }
}
