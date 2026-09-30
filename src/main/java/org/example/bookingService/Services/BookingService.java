package org.example.bookingService.Services;

import org.example.bookingService.Models.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    boolean createBooking(Long customerId, Long roomId, LocalDate startDate, LocalDate endDate);

    boolean updateBooking(Long id, Long roomId, LocalDate startDate, LocalDate endDate);

    boolean deleteBooking(Long id);

    List<Booking> getAllBookings();

    Booking getBookingByCustomerId(Long customerId);
    boolean hasBookingsForCustomer(Long customerId);

}