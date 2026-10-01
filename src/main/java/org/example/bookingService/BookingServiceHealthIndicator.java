package org.example.bookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BookingServiceHealthIndicator implements HealthIndicator {
    public static final Logger logger = LoggerFactory.getLogger(BookingServiceHealthIndicator.class);
    private final RestClient restClient;
    public BookingServiceHealthIndicator(@Value("${customer-service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }
    @Override
    public Health health(){
        try{
            restClient.get().uri("/actuator/health").retrieve().toBodilessEntity();
            return Health.up().build();
        }catch(Exception e){
            logger.warn("booking-service svara inte: {} ", e.getMessage());
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
