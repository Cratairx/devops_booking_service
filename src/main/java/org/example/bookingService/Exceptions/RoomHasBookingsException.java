package org.example.bookingService.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoomHasBookingsException extends RuntimeException {
    public RoomHasBookingsException(Long roomId) {
        super("Room " + roomId + " has bookings and cannot be deleted");
    }
}
