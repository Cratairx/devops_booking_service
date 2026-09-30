package org.example.bookingService.Services.impl;

import jakarta.transaction.Transactional;
import org.example.bookingService.Client.CustomerClient;
import org.example.bookingService.Models.Booking;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.BookingRepository;
import org.example.bookingService.Repositories.RoomRepository;
import org.example.bookingService.Services.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Service

public class BookingServiceImpl implements BookingService {
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, CustomerClient customerClient) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.customerClient = customerClient;
    }

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerClient customerClient;

    @Override
    @Transactional
    public boolean createBooking(Long customerId, Long roomId, LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) return false;

        if (!customerClient.customerExists(customerId)) return false;

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) return false;

        boolean hasConflict = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, customerId);
        if (hasConflict) return false;

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCustomerID(customerId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        bookingRepository.save(booking);
        return true;
    }

    @Override
    @Transactional
    public boolean updateBooking(Long id, Long roomId, LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) return false;

        Booking booking = bookingRepository.findById(id).orElse(null);
        Room room = roomRepository.findById(roomId).orElse(null);

        if (booking == null || room == null) return false;

        boolean hasConflict = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, id);
        if (hasConflict) return false;

        booking.setRoom(room);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        bookingRepository.save(booking);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional
    public Booking getBookingByCustomerId(Long customerId) {
        return bookingRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + customerId));    }

    @Override
    public boolean hasBookingsForCustomer(Long customerId) {
        return bookingRepository.existsBookingForCustomer(customerId);
    }




}

