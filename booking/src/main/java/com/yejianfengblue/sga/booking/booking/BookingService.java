package com.yejianfengblue.sga.booking.booking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class BookingService {

    @NonNull
    private final BookingRepository bookingRepository;

    Booking confirmBooking(Booking booking) {

        Booking confirmedBooking = booking.confirm();
        confirmedBooking = bookingRepository.save(confirmedBooking);

        return confirmedBooking;
    }

    Booking checkInBooking(Booking booking) {

        Booking checkedInBooking = booking.checkIn();
        checkedInBooking = bookingRepository.save(checkedInBooking);

        return checkedInBooking;
    }

    Booking cancelBooking(Booking booking) {

        Booking cancelledBooking = booking.cancel();
        cancelledBooking = bookingRepository.save(cancelledBooking);

        return cancelledBooking;
    }
}
