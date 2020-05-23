package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
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
    LinkDiscoverers linkDiscoverers;

    @Autowired
    BookingRepository bookingRepository;

    private final static String BASE_URL = "http://localhost";

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
    void givenDraftBooking_whenPutConfirm_thenStatusBecomesConfirmed() throws Exception {

        // given
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
    void givenConfirmedBooking_whenPutCheckIn_thenStatusBecomesCheckedIn() throws Exception {

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
    void givenConfirmedBooking_whenDeleteCancel_thenStatusBecomesCancelled() throws Exception {

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

    // illegal state transition

    @Test
    void givenDraftBooking_whenPutCheckIn_thenBadRequest() throws Exception {

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
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", DRAFT, CHECKED_IN)
                ));
    }

    @Test
    void givenConfirmedBooking_whenPutConfirm_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                put(bookingUri + "/confirm")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CONFIRMED, CONFIRMED)
                ));
    }

    @Test
    void givenCheckedInBooking_whenPutConfirm_thenBadRequest() throws Exception {

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
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CHECKED_IN, CONFIRMED)
                ));
    }

    @Test
    void givenCheckedInBooking_whenDeleteCancel_thenBadRequest() throws Exception {

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
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CHECKED_IN, CANCELLED)
                ));
    }

    @Test
    void givenCancelledBooking_whenPutConfirm_thenBadRequest() throws Exception {

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
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CANCELLED, CONFIRMED)
                ));
    }

    @Test
    void givenCancelledBooking_whenPutCheckIn_thenBadRequest() throws Exception {

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
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CANCELLED, CHECKED_IN)
                ));
    }

    @Test
    void givenCancelledBooking_whenDeleteCancel_thenBadRequest() throws Exception {

        // given
        Booking booking = new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester");
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);
        String bookingUri = BASE_URL + "/bookings/" + booking.getId();

        // when
        mockMvc.perform(
                delete(bookingUri + "/cancel")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        String.format("\"Booking status cannot be transited from %s to %s\"", CANCELLED, CANCELLED)
                ));
    }

}
