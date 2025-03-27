package com.sbr.lds.h3;

import com.sbr.common.model.Distance;
import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.uber.h3core.H3Core;
import com.uber.h3core.LengthUnit;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class H3 {
    protected final H3Core h3;
    
    public H3() throws IOException {
        this.h3 = H3Core.newInstance();
    }
    
    public com.uber.h3core.util.LatLng toH3LatLng(LatLng latLng){
        return new com.uber.h3core.util.LatLng(Double.parseDouble(latLng.getLatitude()), Double.parseDouble(latLng.getLongitude()));
    }
    
    public LatLng toLatLng(com.uber.h3core.util.LatLng latLng){
        return LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng));
    }
    
    public Double distance(LatLng start, LatLng end){
        return this.h3.greatCircleDistance(this.toH3LatLng(start), this.toH3LatLng(end), LengthUnit.km);
    }
    
    public String latLngToH3Index(H3Detail h3Detail, Integer defaultResolution){
        h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
        return this.h3.latLngToCellAddress(
                Double.parseDouble(h3Detail.getLocation().getLatitude()),
                Double.parseDouble(h3Detail.getLocation().getLongitude()),
                h3Detail.getResolution());
    }
    
    public H3Detail h3IndexToH3Detail(String h3Index){
        LatLng latLng = this.toLatLng(this.h3.cellToLatLng(h3Index));
        Integer resolution = this.h3.getResolution(h3Index);
        return H3Detail.builder().location(latLng).resolution(resolution).build();
    }
    
    public Map<Integer, List<LatLng>> neighboursMap(H3Detail h3Detail, Integer defaultResolution) {
        AtomicInteger index = new AtomicInteger();
        h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
        String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
        return this.h3.gridDiskDistances(sourceIndex, h3Detail.getRadius())
                       .stream()
                       .map(list-> list.stream()
                                           .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                                           .filter(Optional::isPresent)
                                           .map(Optional::get)
                                           .collect(Collectors.toList()))
                       .collect(Collectors.toMap(s -> index.getAndIncrement(), Function.identity(), (oldV, newV)->newV));
    }
    
    public Map<Integer, List<Distance>> neighboursDistanceMap(H3Detail h3Detail, Integer defaultResolution) {
        AtomicInteger index = new AtomicInteger();
        h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
        String sourceIndex = this.h3.latLngToCellAddress(Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
        return this.h3.gridDiskDistances(sourceIndex, h3Detail.getRadius())
                       .stream()
                       .map(list-> list.stream()
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
                       .collect(Collectors.toMap(s -> index.getAndIncrement(), Function.identity(), (oldV, newV)->newV));
    }
    
    public List<LatLng> neighboursList(H3Detail h3Detail, Integer defaultResolution) {
        h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
        String sourceIndex = this.h3.latLngToCellAddress( Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
        return this.h3.gridDisk(sourceIndex, h3Detail.getRadius())
                       .stream()
                       .map(neighbour -> Optional.ofNullable(this.h3.cellToLatLng(neighbour)).map(latLng -> LatLng.instance(String.valueOf(latLng.lat), String.valueOf(latLng.lng))))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .collect(Collectors.toList());
    }
    
    public List<Distance> neighboursDistanceList(H3Detail h3Detail, Integer defaultResolution) {
        h3Detail.setResolution(Optional.ofNullable(h3Detail.getResolution()).orElse(defaultResolution));
        String sourceIndex = this.h3.latLngToCellAddress( Double.parseDouble(h3Detail.getLocation().getLatitude()), Double.parseDouble(h3Detail.getLocation().getLongitude()), h3Detail.getResolution());
        return this.h3.gridDisk(sourceIndex, h3Detail.getRadius())
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
                       .collect(Collectors.toList());
    }
    
    public List<Distance> neighboursDistanceListWithSort(H3Detail h3Detail, Integer defaultResolution) {
        List<Distance> distances = this.neighboursDistanceList(h3Detail, defaultResolution);
        distances.sort(Comparator.comparingDouble(d -> Double.parseDouble(d.getDistance())));
        return distances;
    }

}
