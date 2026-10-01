package org.example.bookingService.Controllers;

import org.example.bookingService.Models.Booking;
import org.example.bookingService.Models.Room;
import org.example.bookingService.Services.BookingService;
import org.example.bookingService.Services.RoomService;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        boolean success = bookingService.createBooking(customerId, roomId, startDate, endDate);
        return success
                ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.status(HttpStatus.CONFLICT).body("Room unavailable or invalid customer");
    }

    @GetMapping("/available")
    public ResponseEntity<List<Room>> availableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

       if(startDate== null || endDate==null || !startDate.isBefore(endDate)){
           return ResponseEntity.badRequest().build();

       }
        return ResponseEntity.ok(roomService.getAvailableRooms(startDate, endDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Long id,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        boolean success = bookingService.updateBooking(id, roomId, startDate, endDate);
        return success
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).body("Overlapping booking");

    }

    private final Logger log = LoggerFactory.getLogger(BookingController.class);

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        boolean success = bookingService.deleteBooking(id);
        try {
            if (success) {
                log.info("Sucessfully Deleted booking with id {}", id);
                return ResponseEntity.ok().build();

            } else {
                log.warn("Failed to delete booking with id {}", id);
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error("Error while deleting booking with id {}", id, e);
        }

        log.error("something big happend you should not reach this message");
        return null;

    }

    @GetMapping
    public List<Booking> listBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/getbooking/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getBookingByCustomerId(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking);
    }
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsBookings(@RequestParam Long customerId) {
        return ResponseEntity.ok(bookingService.hasBookingsForCustomer(customerId));
    }

}


