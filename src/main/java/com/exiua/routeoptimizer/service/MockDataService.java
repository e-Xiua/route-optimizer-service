package com.exiua.routeoptimizer.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.model.POI;

/**
 * Service to generate mock POI data for testing
 */
@Service
public class MockDataService {
    
    private final Random random = new Random();
    
    // Costa Rica coordinates for realistic locations
    private final List<POI> mockPOIs = Arrays.asList(
        // San José Province
        new POI(1L, "Teatro Nacional", 9.9326, -84.0775),
        new POI(2L, "Mercado Central", 9.9331, -84.0775),
        new POI(3L, "Catedral Metropolitana", 9.9335, -84.0777),
        new POI(4L, "Museo del Oro", 9.9328, -84.0774),
        new POI(5L, "Parque Central San José", 9.9334, -84.0776),
        
        // Cartago Province
        new POI(6L, "Basílica de los Ángeles", 9.8632, -83.9196),
        new POI(7L, "Volcán Irazú", 9.9793, -83.8502),
        new POI(8L, "Jardín Botánico Lankester", 9.8376, -83.8895),
        
        // Alajuela Province  
        new POI(9L, "Volcán Poás", 10.1981, -84.2330),
        new POI(10L, "La Fortuna Arenal", 10.4647, -84.6434),
        new POI(11L, "Catarata La Fortuna", 10.4575, -84.6708),
        new POI(12L, "Tabacón Hot Springs", 10.4808, -84.6578),
        
        // Guanacaste Province
        new POI(13L, "Playa Tamarindo", 10.2994, -85.8397),
        new POI(14L, "Parque Nacional Manuel Antonio", 9.3905, -84.1419),
        new POI(15L, "Playa Conchal", 10.3725, -85.8017),
        new POI(16L, "Liberia Ciudad Blanca", 10.6346, -85.4370),
        
        // Puntarenas Province
        new POI(17L, "Monteverde Cloud Forest", 10.3009, -84.7934),
        new POI(18L, "Isla Tortuga", 9.7833, -84.7667),
        new POI(19L, "Playa Hermosa Jaco", 9.6018, -84.6291),
        new POI(20L, "Parque Nacional Corcovado", 8.5392, -83.5919),
        
        // Limón Province
        new POI(21L, "Puerto Viejo de Talamanca", 9.6533, -82.7572),
        new POI(22L, "Parque Nacional Cahuita", 9.7358, -82.8417),
        new POI(23L, "Tortuguero National Park", 10.5539, -83.5119),
        new POI(24L, "Playa Cocles", 9.6386, -82.7239)
    );
    
    /**
     * Initialize mock POI data with additional details
     */
    public List<POI> getAllMockPOIs() {
        return mockPOIs.stream().map(this::enrichPOI).toList();
    }
    
    /**
     * Get a random subset of POIs for route optimization
     */
    public List<POI> getRandomPOISubset(int count) {
        List<POI> enrichedPOIs = getAllMockPOIs();
        
        // Ensure we don't request more POIs than available
        int actualCount = Math.min(count, enrichedPOIs.size());
        
        return enrichedPOIs.stream()
            .skip(random.nextInt(enrichedPOIs.size() - actualCount + 1))
            .limit(actualCount)
            .toList();
    }
    
    /**
     * Get POIs for a specific route (simulating different route types)
     */
    public List<POI> getPOIsForRoute(String routeType) {
        return switch (routeType.toLowerCase()) {
            case "adventure" -> getAdventurePOIs();
            case "cultural" -> getCulturalPOIs();
            case "beach" -> getBeachPOIs();
            case "nature" -> getNaturePOIs();
            default -> getRandomPOISubset(8);
        };
    }
    
    private List<POI> getAdventurePOIs() {
        return Arrays.asList(
            enrichPOI(mockPOIs.get(6)), // Volcán Irazú
            enrichPOI(mockPOIs.get(8)), // Volcán Poás
            enrichPOI(mockPOIs.get(9)), // La Fortuna Arenal
            enrichPOI(mockPOIs.get(10)), // Catarata La Fortuna
            enrichPOI(mockPOIs.get(16)), // Monteverde Cloud Forest
            enrichPOI(mockPOIs.get(19)) // Parque Nacional Corcovado
        );
    }
    
    private List<POI> getCulturalPOIs() {
        return Arrays.asList(
            enrichPOI(mockPOIs.get(0)), // Teatro Nacional
            enrichPOI(mockPOIs.get(1)), // Mercado Central
            enrichPOI(mockPOIs.get(2)), // Catedral Metropolitana
            enrichPOI(mockPOIs.get(3)), // Museo del Oro
            enrichPOI(mockPOIs.get(5)), // Basílica de los Ángeles
            enrichPOI(mockPOIs.get(15)) // Liberia Ciudad Blanca
        );
    }
    
    private List<POI> getBeachPOIs() {
        return Arrays.asList(
            enrichPOI(mockPOIs.get(12)), // Playa Tamarindo
            enrichPOI(mockPOIs.get(14)), // Playa Conchal
            enrichPOI(mockPOIs.get(18)), // Playa Hermosa Jaco
            enrichPOI(mockPOIs.get(20)), // Puerto Viejo de Talamanca
            enrichPOI(mockPOIs.get(23)) // Playa Cocles
        );
    }
    
    private List<POI> getNaturePOIs() {
        return Arrays.asList(
            enrichPOI(mockPOIs.get(7)), // Jardín Botánico Lankester
            enrichPOI(mockPOIs.get(13)), // Parque Nacional Manuel Antonio
            enrichPOI(mockPOIs.get(16)), // Monteverde Cloud Forest
            enrichPOI(mockPOIs.get(21)), // Parque Nacional Cahuita
            enrichPOI(mockPOIs.get(22)) // Tortuguero National Park
        );
    }
    
    /**
     * Enrich POI with additional random data
     */
    private POI enrichPOI(POI basePOI) {
        POI enriched = new POI(basePOI.getId(), basePOI.getName(), 
                              basePOI.getLatitude(), basePOI.getLongitude());
        
        // Add random data
        enriched.setCategory(getRandomCategory());
        enriched.setRating(3.5 + random.nextDouble() * 1.5); // 3.5 to 5.0
        enriched.setVisitDuration(30 + random.nextInt(180)); // 30 to 210 minutes
        enriched.setPriceLevel(1 + random.nextInt(4)); // 1 to 4
        enriched.setProviderId((long) (1 + random.nextInt(50))); // Random provider ID
        enriched.setDescription(generateDescription(enriched.getName()));
        enriched.setServices(getRandomServices());
        enriched.setOpeningHours(getRandomOpeningHours());
        
        return enriched;
    }
    
    private String getRandomCategory() {
        String[] categories = {"restaurant", "attraction", "hotel", "adventure", "cultural", "nature", "beach"};
        return categories[random.nextInt(categories.length)];
    }
    
    private String getRandomServices() {
        String[] services = {"WiFi", "Parking", "Restaurant", "Tour Guide", "Gift Shop", "Restrooms"};
        int serviceCount = 1 + random.nextInt(3);
        StringBuilder servicesBuilder = new StringBuilder();
        
        for (int i = 0; i < serviceCount; i++) {
            if (i > 0) servicesBuilder.append(", ");
            servicesBuilder.append(services[random.nextInt(services.length)]);
        }
        
        return servicesBuilder.toString();
    }
    
    private String getRandomOpeningHours() {
        String[] hours = {"8:00-17:00", "9:00-18:00", "10:00-16:00", "6:00-18:00", "24 hours"};
        return hours[random.nextInt(hours.length)];
    }
    
    private String generateDescription(String name) {
        return "Discover the beauty and wonder of " + name + 
               ". A must-visit destination in Costa Rica offering unique experiences for all visitors.";
    }
}