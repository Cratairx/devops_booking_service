package org.example.bookingService.Models;

import jakarta.persistence.*;

import org.example.bookingService.ENUMS.RoomType;

import java.util.Objects;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    public Room() {

    }

    public Room(Long id, String roomNumber, RoomType roomType) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    public Room(String number, RoomType roomType) {
        this.roomNumber = number;
        this.roomType = roomType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return id != null && id.equals(room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}