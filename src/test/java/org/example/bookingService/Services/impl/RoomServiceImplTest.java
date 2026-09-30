package org.example.bookingService.Services.impl;

import org.example.bookingService.ENUMS.RoomType;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomServiceImpl(roomRepository);
    }

    @Test
    void getAllRoomsReturnsRepositoryResult() {
        List<Room> rooms = List.of(new Room(1L, "101", RoomType.SINGLE));
        when(roomRepository.findAll()).thenReturn(rooms);

        assertThat(roomService.getAllRooms()).isEqualTo(rooms);
    }

    @Test
    void saveRoomPersistsAndReturnsRoom() {
        Room room = new Room(null, "202", RoomType.DOUBLE);
        Room saved = new Room(1L, "202", RoomType.DOUBLE);
        when(roomRepository.save(room)).thenReturn(saved);

        assertThat(roomService.saveRoom(room)).isEqualTo(saved);
    }

    @Test
    void deleteRoomDeletesAndReturnsTrueWhenExists() {
        when(roomRepository.existsById(1L)).thenReturn(true);

        assertThat(roomService.deleteRoom(1L)).isTrue();
        verify(roomRepository).deleteById(1L);
    }

    @Test
    void deleteRoomReturnsFalseWhenNotExists() {
        when(roomRepository.existsById(1L)).thenReturn(false);

        assertThat(roomService.deleteRoom(1L)).isFalse();
        verify(roomRepository, never()).deleteById(any());
    }

    @Test
    void getRoomByIdReturnsRoomWhenFound() {
        Room room = new Room(1L, "101", RoomType.SINGLE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThat(roomService.getRoomById(1L)).isEqualTo(room);
    }

    @Test
    void getRoomByIdThrowsWhenNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("1");
    }

    @Test
    void saveRoomByIdDelegatesExistsCheck() {
        when(roomRepository.existsById(1L)).thenReturn(true);

        assertThat(roomService.saveRoom(1L)).isTrue();
    }

    @Test
    void getAvailableRoomsDelegatesToRepositoryQuery() {
        LocalDate start = LocalDate.of(2026, 9, 10);
        LocalDate end = LocalDate.of(2026, 9, 12);
        List<Room> available = List.of(new Room(2L, "303", RoomType.DOUBLE));
        when(roomRepository.findAvailableRooms(start, end)).thenReturn(available);

        assertThat(roomService.getAvailableRooms(start, end)).isEqualTo(available);
    }
}