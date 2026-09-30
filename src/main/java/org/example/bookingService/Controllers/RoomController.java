package org.example.bookingService.Controllers;

import org.example.bookingService.ENUMS.RoomType;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Services.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> listRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(
            @RequestParam String roomNumber,
            @RequestParam RoomType roomType) {

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        Room saved = roomService.saveRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> editRoom(
            @PathVariable Long id,
            @RequestParam RoomType roomType,
            @RequestParam String roomNumber) {

        Room room = roomService.getRoomById(id);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        return ResponseEntity.ok(roomService.saveRoom(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        boolean success = roomService.deleteRoom(id);
        return success
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}