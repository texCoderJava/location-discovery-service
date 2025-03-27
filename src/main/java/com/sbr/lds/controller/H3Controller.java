package com.sbr.lds.controller;

import com.sbr.common.model.Distance;
import com.sbr.common.model.H3Detail;
import com.sbr.common.model.LatLng;
import com.sbr.lds.distance.Haversine;
import com.sbr.lds.h3.H3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("h3")
public class H3Controller {
    
    @Value("${location.ranges.latrange}")
    private String latRange;
    @Value("${location.ranges.longrange}")
    private String longRange;
    @Value("${h3.default.resolution}")
    private Integer h3Resolution;
    @Autowired
    private H3 h3;
    
    @GetMapping("distance")
    public Distance distance() {
        String[] lats = Arrays.stream(this.latRange.trim().split("::")).map(String::trim).toArray(String[]::new);
        String[] longs = Arrays.stream(this.longRange.trim().split("::")).map(String::trim).toArray(String[]::new);
        LatLng start = LatLng.instance(lats[0], longs[0]);
        LatLng end = LatLng.instance(lats[1], longs[1]);
        return Distance.instance(String.valueOf(Haversine.distance(start, end)), start, end, null, null);
    }
    
    @GetMapping("index")
    public String latLngToH3Index(@RequestBody H3Detail h3Detail) {
        return this.h3.latLngToH3Index(h3Detail, this.h3Resolution);
    }
    
    @GetMapping("detail/{h3Index}")
    public H3Detail h3IndexToH3Detail(@PathVariable String h3Index) {
        return this.h3.h3IndexToH3Detail(h3Index);
    }
    
    @PostMapping("neighbours/map")
    public Map<Integer, List<LatLng>> neighboursMap(@RequestBody H3Detail h3Detail) {
        return this.h3.neighboursMap(h3Detail, this.h3Resolution);
    }
    
    @PostMapping("neighbours/distance/map")
    public Map<Integer, List<Distance>> neighboursDistanceMap(@RequestBody H3Detail h3Detail) {
        return this.h3.neighboursDistanceMap(h3Detail, this.h3Resolution);
    }
    
    @PostMapping("neighbours")
    public List<LatLng> neighbours(@RequestBody H3Detail h3Detail) {
        return this.h3.neighboursList(h3Detail, this.h3Resolution);
    }
    
    @PostMapping("neighbours/distance")
    public List<Distance> neighboursDistance(@RequestBody H3Detail h3Detail) {
        return this.h3.neighboursDistanceList(h3Detail, this.h3Resolution);
    }
    
    @PostMapping("neighbours/distance/sort")
    public List<Distance> neighboursDistanceWithSort(@RequestBody H3Detail h3Detail) {
        return this.h3.neighboursDistanceListWithSort(h3Detail, this.h3Resolution);
    }
    
}