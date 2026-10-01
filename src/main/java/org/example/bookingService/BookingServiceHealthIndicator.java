package org.example.bookingService;

import org.example.bookingService.Repositories.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BookingServiceHealthIndicator implements HealthIndicator {
    private final RoomRepository roomRepository;

    public BookingServiceHealthIndicator(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Health health() {
        try {
            long rooms = roomRepository.count();
            return Health.up().withDetail("rooms", rooms).build();
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
