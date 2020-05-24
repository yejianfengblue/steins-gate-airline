package com.yejianfengblue.sga.booking.booking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class BookingService {

    @NonNull
    private final BookingRepository bookingRepository;

    @Transactional
    Booking confirmBooking(Booking booking) {

        Booking confirmedBooking = booking.confirm();
        confirmedBooking = bookingRepository.save(confirmedBooking);

        return confirmedBooking;
    }

    @Transactional
    Booking checkInBooking(Booking booking) {

        Booking checkedInBooking = booking.checkIn();
        checkedInBooking = bookingRepository.save(checkedInBooking);

        return checkedInBooking;
    }

    @Transactional
    Booking cancelBooking(Booking booking) {

        Booking cancelledBooking = booking.cancel();
        cancelledBooking = bookingRepository.save(cancelledBooking);

        return cancelledBooking;
    }
}
