package org.example.bookingService.Services.impl;

import org.example.bookingService.Exceptions.RoomHasBookingsException;
import org.example.bookingService.Exceptions.RoomNotFoundException;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.BookingRepository;
import org.example.bookingService.Repositories.RoomRepository;
import org.example.bookingService.Services.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service

public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room roomExists(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public boolean deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            return false;
        }
        if (bookingRepository.existsByRoomId(id)) {
            throw new RoomHasBookingsException(id);
        }
        roomRepository.deleteById(id);
        return true;
    }

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    @Override
    public boolean roomExists(Long id) {
        return  roomRepository.existsById(id);
    }

    @Override
    public List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRooms(startDate, endDate);
    }

}
