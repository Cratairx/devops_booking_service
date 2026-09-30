package org.example.bookingService.Repositories;

import org.example.bookingService.Models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.id <> :excludeId " +
            "AND b.startDate < :endDate " +
            "AND b.endDate > :startDate")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );
    @Query("SELECT COUNT(booking) > 0 FROM Booking booking WHERE booking.customerID = :customerId")
    boolean existsBookingForCustomer(@Param("customerId") Long customerId);

}
