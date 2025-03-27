package com.sbr.lds.h3;

import com.sbr.common.model.Distance;
import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.sbr.common.model.Pair;
import com.uber.h3core.H3Core;
import com.uber.h3core.LengthUnit;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class H3Reactive {
    protected final H3Core h3;
    public H3Reactive() throws IOException {
        this.h3 = H3Core.newInstance();
    }
    
    public com.uber.h3core.util.LatLng toH3LatLng(LatLng latLng){
        return new com.uber.h3core.util.LatLng(Double.parseDouble(latLng.getLatitude()), Double.parseDouble(latLng.getLongitude()));
    }
    
    public LatLng toLatLng(com.uber.h3core.util.LatLng latLng){
        return LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng));
    }
    
    public Mono<Double> distance(Mono<LatLng> startMono, Mono<LatLng> endMono){
        return Mono.create(sink -> startMono.subscribe(start -> endMono.subscribe(end -> sink.success(this.h3.greatCircleDistance(this.toH3LatLng(start), this.toH3LatLng(end), LengthUnit.km)))));
    }
    
    public Mono<String> latLngToH3Index(Mono<H3Detail> h3DetailMono, Integer defaultResolution){
        return Mono.create(sink ->
            h3DetailMono.subscribe(h3Detail -> {
                h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                String h3Index = this.h3.latLngToCellAddress(
                        Double.parseDouble(h3Detail.getLocation().getLatitude()),
                        Double.parseDouble(h3Detail.getLocation().getLongitude()),
                        h3Detail.getResolution());
                sink.success(h3Index);
            }));
    }
    
    public Mono<H3Detail> h3IndexToH3Detail(Mono<String> h3IndexMono){
        return Mono.create(sink ->
            h3IndexMono.subscribe(h3Index -> {
                LatLng latLng = this.toLatLng(this.h3.cellToLatLng(h3Index));
                Integer resolution = this.h3.getResolution(h3Index);
                H3Detail h3Detail = H3Detail.builder().location(latLng).resolution(resolution).build();
                sink.success(h3Detail);
            }));
    }
    
    public Flux<Pair<Integer, List<LatLng>>> neighboursMap(Mono<H3Detail> h3DetailMono, Integer defaultResolution) {
        return Flux.create(sink->
            h3DetailMono.subscribe(h3Detail -> {
                    h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                    String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
                    AtomicInteger index = new AtomicInteger();
                    this.h3.gridDiskDistances(sourceIndex, h3Detail.getRadius())
                            .stream()
                            .map(list -> list.stream()
                                                 .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                                                 .filter(Optional::isPresent)
                                                 .map(Optional::get)
                                                 .collect(Collectors.toList()))
                            .forEach(latLngList -> sink.next(Pair.instance(index.getAndIncrement(), latLngList)));
                    sink.complete();
                }));
    }
    
    public Flux<Pair<Integer, List<Distance>>> neighboursDistanceMap(Mono<H3Detail> h3DetailMono, Integer defaultResolution) {
        return Flux.create(sink->
            h3DetailMono.subscribe(h3Detail -> {
                h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
                AtomicInteger index = new AtomicInteger();
                this.h3.gridDiskDistances(sourceIndex, h3Detail.getRadius())
                        .stream()
                        .map(list -> list.stream()
                                             .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                                             .filter(Optional::isPresent)
                                             .map(Optional::get)
                                             .map(neighbour ->  Distance.instance(
                                                     String.valueOf(this.h3.greatCircleDistance(this.toH3LatLng(h3Detail.getLocation()), this.toH3LatLng(neighbour), LengthUnit.km)),
                                                     h3Detail.getLocation(),
                                                     neighbour,
                                                     sourceIndex,
                                                     this.h3.latLngToCellAddress(
                                                             Double.parseDouble(neighbour.getLatitude()),
                                                             Double.parseDouble(neighbour.getLongitude()),
                                                             h3Detail.getResolution()))
                                             )
                                             .collect(Collectors.toList()))
                        .forEach(distanceList -> sink.next(Pair.instance(index.getAndIncrement(), distanceList)));
                sink.complete();
            }));
    }
    
    public Flux<LatLng> neighboursList(Mono<H3Detail> h3DetailMono, Integer defaultResolution) {
        return Flux.create(sink->
            h3DetailMono.subscribe(h3Detail -> {
                    h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                    String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
                    this.h3.gridDisk(sourceIndex, h3Detail.getRadius())
                            .stream()
                            .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sink::next);
                    sink.complete();
                }));
        
        
    }
    
    public Flux<Distance> neighboursDistanceList(Mono<H3Detail> h3DetailMono, Integer defaultResolution) {
        return Flux.create(sink->
            h3DetailMono.subscribe(h3Detail -> {
                h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
                this.h3.gridDisk(sourceIndex, h3Detail.getRadius())
                        .stream()
                        .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(neighbour ->  Distance.instance(
                                String.valueOf(this.h3.greatCircleDistance(this.toH3LatLng(h3Detail.getLocation()), this.toH3LatLng(neighbour), LengthUnit.km)),
                                h3Detail.getLocation(),
                                neighbour,
                                sourceIndex,
                                this.h3.latLngToCellAddress(
                                        Double.parseDouble(neighbour.getLatitude()),
                                        Double.parseDouble(neighbour.getLongitude()),
                                        h3Detail.getResolution()))
                        )
                        .forEach(sink::next);
                sink.complete();
            }));
    }
    
    public Flux<Distance> neighboursDistanceListWithSort(Mono<H3Detail> h3DetailMono, Integer defaultResolution) {
        List<Distance> distances = new ArrayList<>();
        return Flux.create(sink ->
            h3DetailMono
                    .doOnNext(h3Detail -> {
                            h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
                            String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
                            distances.addAll(this.h3.gridDisk(sourceIndex, h3Detail.getRadius())
                                                     .stream()
                                                     .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                                                     .filter(Optional::isPresent)
                                                     .map(Optional::get)
                                                     .map(neighbour ->  Distance.instance(
                                                             String.valueOf(this.h3.greatCircleDistance(this.toH3LatLng(h3Detail.getLocation()), this.toH3LatLng(neighbour), LengthUnit.km)),
                                                             h3Detail.getLocation(),
                                                             neighbour,
                                                             sourceIndex,
                                                             this.h3.latLngToCellAddress(
                                                                     Double.parseDouble(neighbour.getLatitude()),
                                                                     Double.parseDouble(neighbour.getLongitude()),
                                                                     h3Detail.getResolution()))
                                                     )
                                                     .collect(Collectors.toList()));
                    })
                    .doOnSuccess(h3Detail -> {
                        distances.sort(Comparator.comparingDouble(d -> Double.parseDouble(d.getDistance())));
                        distances.forEach(sink::next);
                    })
                    .doFinally(signal -> sink.complete())
                    .subscribe());
    }
}
