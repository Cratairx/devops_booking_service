package org.example.bookingService.Config;
import org.example.bookingService.ENUMS.RoomType;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RoomConfig implements CommandLineRunner {

    private final RoomRepository roomRepository;
    public RoomConfig(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            roomRepository.saveAll(List.of(
                    new Room( null,"101", RoomType.SINGLE),
                    new Room ( null,"102", RoomType.SINGLE),
                    new Room( null,"103", RoomType.DOUBLE)
            ));
        }
   }
}
