package org.example.bookingService.Repositories;

import org.example.bookingService.Models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
        SELECT r FROM Room r
        WHERE r NOT IN (
            SELECT b.room FROM Booking b
            WHERE b.startDate < :endDate
            AND b.endDate > :startDate
        )
    """)

    List<Room> findAvailableRooms(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}