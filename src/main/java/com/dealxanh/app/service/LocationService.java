package com.dealxanh.app.service;

import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.entity.UserAddress;
import com.dealxanh.app.repository.UserAddressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Xác định vị trí người dùng qua IP geolocation (dùng ipapi.co).
 * Fallback: dùng địa chỉ đăng ký của user.
 * Cung cấp công thức Haversine để tính khoảng cách.
 */
@Service
public class LocationService {

    @Autowired
    private UserAddressRepository userAddressRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Lấy lat/lng của user: 1) IP geolocation, 2) UserAddress mặc định, 3) null */
    public double[] getUserLocation(jakarta.servlet.http.HttpServletRequest request, User user) {
        // 1. Thử IP geolocation
        double[] ipLoc = getLocationFromIP(request);
        if (ipLoc != null) return ipLoc;

        // 2. Fallback: địa chỉ đăng ký mặc định
        if (user != null) {
            try {
                var addresses = userAddressRepository.findByUserUserIdOrderByIsDefaultDesc(user.getUserId());
                if (addresses != null && !addresses.isEmpty()) {
                    UserAddress addr = addresses.get(0);
                    double[] geo = geocodeAddress(addr.getFullAddress());
                    if (geo != null) return geo;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    /** Lấy tọa độ từ IP qua ipapi.co (free, 1000 req/day) */
    private double[] getLocationFromIP(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String clientIP = getClientIP(request);
            if (clientIP == null || clientIP.equals("127.0.0.1") || clientIP.equals("0:0:0:0:0:0:0:1")) {
                return null; // localhost - fallback to registered address
            }
            String url = "https://ipapi.co/" + clientIP + "/json/";
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(3))
                .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(3))
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> data = mapper.readValue(resp.body(), Map.class);
                if (data != null && data.get("latitude") != null && data.get("longitude") != null) {
                    double lat = ((Number) data.get("latitude")).doubleValue();
                    double lon = ((Number) data.get("longitude")).doubleValue();
                    System.out.println("IP Geolocation: " + clientIP + " -> " + lat + ", " + lon);
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.out.println("IP Geolocation failed: " + e.getMessage());
        }
        return null;
    }

    /** Geocode địa chỉ text thành tọa độ dùng Nominatim (OpenStreetMap, free) */
    public double[] geocodeAddress(String address) {
        if (address == null || address.isEmpty()) return null;
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q="
                + java.net.URLEncoder.encode(address, "UTF-8");
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("User-Agent", "DealXanh/1.0")
                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> results = mapper.readValue(resp.body(), java.util.List.class);
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> r = results.get(0);
                    double lat = Double.parseDouble((String) r.get("lat"));
                    double lon = Double.parseDouble((String) r.get("lon"));
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.out.println("Geocode failed for: " + address + " - " + e.getMessage());
        }
        return null;
    }

    /** Công thức Haversine - khoảng cách giữa 2 điểm (km) */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Khoảng cách từ user đến store (km), hoặc -1 nếu không tính được */
    public double distanceToStore(double[] userLoc, Store store) {
        if (userLoc == null || store == null) return -1;
        if (store.getLatitude() == null || store.getLongitude() == null) return -1;
        return distanceKm(userLoc[0], userLoc[1], store.getLatitude(), store.getLongitude());
    }

    private String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
