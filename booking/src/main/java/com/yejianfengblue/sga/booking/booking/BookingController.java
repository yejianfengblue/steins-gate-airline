package com.yejianfengblue.sga.booking.booking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
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

    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity confirm(@PathVariable String id) {

        Optional<Booking> foundBooking = this.bookingRepository.findById(id);
        if (foundBooking.isPresent()) {

            Booking booking = foundBooking.get();
            if (valid(booking.getStatus(), CONFIRMED)) {

                booking = bookingRepository.save(booking.confirm());
                return ResponseEntity.ok(booking);
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
            if (valid(booking.getStatus(), CHECKED_IN)) {

                booking = bookingRepository.save(booking.checkIn());
                return ResponseEntity.ok(booking);
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
            if (valid(booking.getStatus(), CANCELLED)) {

                booking = bookingRepository.save(booking.cancel());
                return ResponseEntity.ok(booking);
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
