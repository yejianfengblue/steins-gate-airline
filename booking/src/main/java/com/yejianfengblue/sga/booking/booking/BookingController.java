package com.yejianfengblue.sga.booking.booking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Optional;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;

@RepositoryRestController
@RequiredArgsConstructor
public class BookingController {

    @NonNull
    private final BookingRepository bookingRepository;

    @NonNull
    private final BookingService bookingService;

    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity confirm(@PathVariable String id) {

        Optional<Booking> foundBooking = this.bookingRepository.findById(id);
        if (foundBooking.isPresent()) {

            Booking booking = foundBooking.get();
            if (booking.getStatus() == CONFIRMED) {
                // if booking status is already CONFIRMED, do nothing
                return ResponseEntity.ok(EntityModel.of(booking));
            } else if (valid(booking.getStatus(), CONFIRMED)) {

                booking = bookingService.confirmBooking(booking);
                return ResponseEntity.ok(EntityModel.of(booking));
            } else {

                return ResponseEntity
                        .badRequest()
                        .body(String.format("Booking status cannot be transited from %s to %s",
                                booking.getStatus(), CONFIRMED));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/bookings/{id}/check-in")
    public ResponseEntity checkIn(@PathVariable String id) {

        Optional<Booking> foundBooking = this.bookingRepository.findById(id);
        if (foundBooking.isPresent()) {

            Booking booking = foundBooking.get();
            if (booking.getStatus() == CHECKED_IN) {
                // if booking status is already CHECKED_IN, do nothing
                return ResponseEntity.ok(EntityModel.of(booking));
            } else if (valid(booking.getStatus(), CHECKED_IN)) {

                booking = bookingService.checkInBooking(booking);
                return ResponseEntity.ok(EntityModel.of(booking));
            } else {

                return ResponseEntity
                        .badRequest()
                        .body(String.format("Booking status cannot be transited from %s to %s",
                                booking.getStatus(), CHECKED_IN));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/bookings/{id}/cancel")
    public ResponseEntity cancel(@PathVariable String id) {

        Optional<Booking> foundBooking = this.bookingRepository.findById(id);
        if (foundBooking.isPresent()) {

            Booking booking = foundBooking.get();
            if (booking.getStatus() == CANCELLED) {
                // if booking status is already CANCELLED, do nothing
                return ResponseEntity.ok(EntityModel.of(booking));
            } else if (valid(booking.getStatus(), CANCELLED)) {

                booking = bookingService.cancelBooking(booking);
                return ResponseEntity.ok(EntityModel.of(booking));
            } else {

                return ResponseEntity
                        .badRequest()
                        .body(String.format("Booking status cannot be transited from %s to %s",
                                booking.getStatus(), CANCELLED));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
