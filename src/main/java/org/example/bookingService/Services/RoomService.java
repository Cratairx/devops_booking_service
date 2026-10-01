package org.example.bookingService.Services;

import org.example.bookingService.Models.Room;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    List<Room> getAllRooms();

    Room roomExists(Room room);

    boolean deleteRoom(Long id);

    Room getRoomById(Long id);

    boolean roomExists(Long id);

    List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate);

}