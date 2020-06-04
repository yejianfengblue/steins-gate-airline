package com.yejianfengblue.sga.booking.booking;

import com.yejianfengblue.sga.booking.inventory.Inventory;
import com.yejianfengblue.sga.booking.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class BookingService {

    private final BookingRepository bookingRepository;

    private final InventoryService inventoryService;

    @Transactional
    Booking confirmBooking(Booking booking) {

        Optional<Inventory> foundInventory = inventoryService.findInventory(booking.getCarrier(), booking.getFltNum(), booking.getFltDate());
        if (foundInventory.isPresent()) {

            if (foundInventory.get().getAvailable(booking.getSegOrig(), booking.getSegDest()) >= 1) {

                inventoryService.subtractInventory(foundInventory.get(),
                        booking.getSegOrig(),
                        booking.getSegDest(),
                        1);
                Booking confirmedBooking = booking.confirm();
                confirmedBooking = bookingRepository.save(confirmedBooking);
                return confirmedBooking;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No enough inventory");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Inventory not found");
        }
    }

    @Transactional
    Booking checkInBooking(Booking booking) {

        Booking checkedInBooking = booking.checkIn();
        checkedInBooking = bookingRepository.save(checkedInBooking);

        return checkedInBooking;
    }

    @Transactional
    Booking cancelBooking(Booking booking) {

        if (booking.getStatus().equals(Booking.Status.CONFIRMED)) {

            Optional<Inventory> foundInventory = inventoryService.findInventory(booking.getCarrier(), booking.getFltNum(), booking.getFltDate());
            if (foundInventory.isPresent()) {

                inventoryService.addInventory(foundInventory.get(),
                        booking.getSegOrig(),
                        booking.getSegDest(),
                        1);
            } else {
                log.warn("Cancel confirmed booking but inventory not found. Booking = {}", booking);
            }
        }

        Booking cancelledBooking = booking.cancel();
        cancelledBooking = bookingRepository.save(cancelledBooking);

        return cancelledBooking;
    }
}
