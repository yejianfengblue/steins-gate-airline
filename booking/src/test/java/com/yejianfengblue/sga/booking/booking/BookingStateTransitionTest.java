package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.booking.inventory.Inventory;
import com.yejianfengblue.sga.booking.inventory.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class BookingStateTransitionTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookingRepository bookingRepository;

    @MockBean
    private InventoryService inventoryService;

    private final static String BASE_URL = "http://localhost";

    @AfterEach
    void cleanTestData() {
        bookingRepository.deleteAll();
    }

    // GET

    @Test
    void givenDraftBooking_whenGet_thenLinksContainConfirmAndCancelButNoCheckIn() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(DRAFT);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                get(bookingUri)
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value(DRAFT.toString()))
                // then
                .andExpect(jsonPath("$._links.confirm.href", is(bookingUri + "/confirm")))
                .andExpect(jsonPath("$._links.cancel.href", is(bookingUri + "/cancel")))
                .andExpect(jsonPath("$._links.check-in.href").doesNotExist());
    }

    @Test
    void givenConfirmedBooking_whenGet_thenLinksContainCheckInAndCancelButNoConfirm() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                get(bookingUri)
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value(CONFIRMED.toString()))
                // then
                .andExpect(jsonPath("$._links.check-in.href", is(bookingUri + "/check-in")))
                .andExpect(jsonPath("$._links.cancel.href", is(bookingUri + "/cancel")))
                .andExpect(jsonPath("$._links.confirm.href").doesNotExist());
    }

    @Test
    void givenCheckedInBooking_whenGet_thenLinksNotContainConfirmOrCheckInOrCancel() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking.checkIn();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CHECKED_IN);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                get(bookingUri)
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value(CHECKED_IN.toString()))
                // then
                .andExpect(jsonPath("$._links.confirm.href").doesNotExist())
                .andExpect(jsonPath("$._links.check-in.href").doesNotExist())
                .andExpect(jsonPath("$._links.cancel.href").doesNotExist());
    }

    @Test
    void givenCancelledBooking_whenGet_thenLinksNotContainConfirmOrCheckInOrCancel() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                get(bookingUri)
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value(CANCELLED.toString()))
                // then
                .andExpect(jsonPath("$._links.confirm.href").doesNotExist())
                .andExpect(jsonPath("$._links.check-in.href").doesNotExist())
                .andExpect(jsonPath("$._links.cancel.href").doesNotExist());
    }

    // legal state transition

    @Test
    void givenDraftBookingAndEnoughInventory_whenConfirm_thenStatusBecomesConfirmed() throws Exception {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // given
        given(this.inventoryService.findInventory(carrier, fltNum, fltDate))
                .willReturn(Optional.of(new Inventory(carrier, fltNum, fltDate, 1)));
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(DRAFT);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                // then
                .andExpect(jsonPath("$.status").value(CONFIRMED.toString()));
    }

    @Test
    void givenConfirmedBooking_whenCheckIn_thenStatusBecomesCheckedIn() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/check-in")
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                // then
                .andExpect(jsonPath("$.status").value(CHECKED_IN.toString()));
    }

    @Test
    void givenConfirmedBooking_whenCancel_thenStatusBecomesCancelled() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                delete(bookingUri + "/cancel")
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                // then
                .andExpect(jsonPath("$.status").value(CANCELLED.toString()));
    }

    @Test
    void givenExistingInventoryAndConfirmedBooking_whenCancel_thenStatusBecomesCancelled() throws Exception {

        String carrier = "SG";
        String fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // given
        given(this.inventoryService.findInventory(carrier, fltNum, fltDate))
                .willReturn(Optional.of(new Inventory(carrier, fltNum, fltDate, 0)));
        Booking booking = new Booking(carrier, fltNum, fltDate, "HKG", "TPE", "Tester");
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                delete(bookingUri + "/cancel")
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestMediaTypes.HAL_JSON))
                // then
                .andExpect(jsonPath("$.status").value(CANCELLED.toString()));
    }

    // illegal state transition

    @Test
    void givenDraftBooking_whenCheckIn_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(DRAFT);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/check-in")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(
                        String.format("Booking status cannot be transited from %s to %s", DRAFT, CHECKED_IN)
                ));
    }

    @Test
    void givenCheckedInBooking_whenConfirm_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking.checkIn();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CHECKED_IN);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(
                        String.format("Booking status cannot be transited from %s to %s", CHECKED_IN, CONFIRMED)
                ));
    }

    @Test
    void givenCheckedInBooking_whenCancel_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking.checkIn();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CHECKED_IN);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                delete(bookingUri + "/cancel")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(
                        String.format("Booking status cannot be transited from %s to %s", CHECKED_IN, CANCELLED)
                ));
    }

    @Test
    void givenCancelledBooking_whenConfirm_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(
                        String.format("Booking status cannot be transited from %s to %s", CANCELLED, CONFIRMED)
                ));
    }

    @Test
    void givenCancelledBooking_whenCheckIn_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/check-in")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(
                        String.format("Booking status cannot be transited from %s to %s", CANCELLED, CHECKED_IN)
                ));
    }

    // fail to confirm booking due to unsatisfied inventory

    @Test
    void givenDraftBookingAndInventoryNotExist_whenConfirm_thenInternalServerErrorWithReasonInventoryNotFound() throws Exception {

        String carrier = "SG";
        String fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // given
        given(this.inventoryService.findInventory(carrier, fltNum, fltDate))
                .willReturn(Optional.empty());
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(DRAFT);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .with(jwt()))
                // then
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("Inventory not found"));
    }

    @Test
    void givenDraftBookingAndNoEnoughInventory_whenConfirm_thenBadRequestWithReasonNoEnoughInventory() throws Exception {

        String carrier = "SG";
        String fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // given
        given(this.inventoryService.findInventory(carrier, fltNum, fltDate))
                .willReturn(Optional.of(new Inventory(carrier, fltNum, fltDate, 0)));
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(DRAFT);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .with(jwt()))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("No enough inventory"));
    }
}
