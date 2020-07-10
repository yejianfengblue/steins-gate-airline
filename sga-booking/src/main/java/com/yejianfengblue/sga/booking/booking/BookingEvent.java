package com.yejianfengblue.sga.booking.booking;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event of {@link Booking}
 */
@Value
public class BookingEvent {

    Booking booking;

    Type type;

    UUID id;

    Instant timestamp;

    static BookingEvent of(Booking booking, Type type) {
        return new BookingEvent(booking, type, UUID.randomUUID(), Instant.now());
    }

    private BookingEvent(Booking booking, Type type, UUID id, Instant timestamp) {

        this.booking = booking;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
    }

    enum Type {
        CONFIRM,
        CHECK_IN,
        CANCEL,
    }
}