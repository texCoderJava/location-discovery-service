package com.sbr.lds.distance;

import com.sbr.common.model.LatLng;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Haversine {
    
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    
    public static double distance(LatLng start, LatLng end) {
        
        double startLat = Double.parseDouble(start.getLatitude());
        double startLong = Double.parseDouble(start.getLongitude());
        
        double endLat = Double.parseDouble(end.getLatitude());
        double endLong = Double.parseDouble(end.getLongitude());
        
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));
        
        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);
        
        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}