package org.example.bookingService.Services.impl;

import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.RoomRepository;
import org.example.bookingService.Services.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service

public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room saveRoom(Room room) {
       return roomRepository.save(room);
    }

    @Override
    public boolean deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    @Override
    public boolean saveRoom(Long id) {
        return  roomRepository.existsById(id);
    }

    @Override
    public List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRooms(startDate, endDate);
    }

}
