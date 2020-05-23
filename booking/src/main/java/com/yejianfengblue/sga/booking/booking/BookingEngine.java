package com.yejianfengblue.sga.booking.booking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingEngine {

    @NonNull
    private final StreamBridge streamBridge;

    static final String BOOKING_EVENT_OUT_BINDING = "booking-out-0";

    @Async
    @TransactionalEventListener
    public void handleBookingDomainEvent(BookingEvent bookingEvent) {

        streamBridge.send(BOOKING_EVENT_OUT_BINDING, bookingEvent);

        log.info("Send to booking : {}", bookingEvent);
    }
}
