package org.example.bookingService.Services.impl;


import org.example.bookingService.Client.CustomerClient;
import org.example.bookingService.Models.Booking;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Repositories.BookingRepository;
import org.example.bookingService.Repositories.RoomRepository;
import org.example.bookingService.Services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service

public class BookingServiceImpl implements BookingService {

    private static final Logger log = (Logger) LoggerFactory.getLogger(BookingServiceImpl.class);

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
        try {
            return doCreateBooking(customerId, roomId, startDate, endDate);
        } catch (RuntimeException e) {
            log.error("Failed to create booking (customerId={}, roomId={}, startDate={}, endDate={})",
                    customerId, roomId, startDate, endDate, e);
            throw e; // rethrow so the transaction rolls back and the caller gets a 500, not a misleading 409
        }
    }

    private boolean doCreateBooking(Long customerId, Long roomId, LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            log.warn("Booking rejected, invalid dates (customerId={}, roomId={}, startDate={}, endDate={})",
                    customerId, roomId, startDate, endDate);
            return false;
        }

        if (!customerClient.customerExists(customerId)) {
            log.warn("Booking rejected, customer {} not found or customer-service unavailable (roomId={})",
                    customerId, roomId);
            return false;
        }

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.warn("Booking rejected, room {} not found (customerId={})", roomId, customerId);
            return false;
        }

        boolean hasConflict = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, customerId);
        if (hasConflict) {
            log.warn("Booking rejected, room {} already booked for {} to {} (customerId={})",
                    roomId, startDate, endDate, customerId);
            return false;
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCustomerID(customerId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking created (bookingId={}, customerId={}, roomId={}, startDate={}, endDate={})",
                saved.getId(), customerId, roomId, startDate, endDate);
        return true;
    }

    @Override
    @Transactional
    public boolean updateBooking(Long id, Long roomId, LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
                log.warn("Booking update rejected, invalid dates (bookingId={}, roomId={}, startDate={}, endDate={})",
                        id, roomId, startDate, endDate);
                return false;
            }

            Booking booking = bookingRepository.findById(id).orElse(null);
            Room room = roomRepository.findById(roomId).orElse(null);

            if (booking == null || room == null) {
                log.warn("Booking update rejected, booking {} or room {} not found", id, roomId);
                return false;
            }

            boolean hasConflict = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, id);
            if (hasConflict) {
                log.warn("Booking update rejected, room {} already booked for {} to {} (bookingId={})",
                        roomId, startDate, endDate, id);
                return false;
            }

            booking.setRoom(room);
            booking.setStartDate(startDate);
            booking.setEndDate(endDate);
            bookingRepository.save(booking);
            log.info("Booking updated (bookingId={}, roomId={}, startDate={}, endDate={})",
                    id, roomId, startDate, endDate);
            return true;
        } catch (RuntimeException e) {
            log.error("Failed to update booking (bookingId={}, roomId={}, startDate={}, endDate={})",
                    id, roomId, startDate, endDate, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteBooking(Long id) {
        try {
            if (bookingRepository.existsById(id)) {
                bookingRepository.deleteById(id);
                log.info("Booking deleted (bookingId={})", id);
                return true;
            }
            log.warn("Booking delete rejected, booking {} not found", id);
            return false;
        } catch (RuntimeException e) {
            log.error("Failed to delete booking (bookingId={})", id, e);
            throw e;
        }
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional
    public Booking getBookingByCustomerId(Long customerId) {
        return bookingRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + customerId));
    }

    @Override
    public boolean hasBookingsForCustomer(Long customerId) {
        return bookingRepository.existsBookingForCustomer(customerId);
    }
}
