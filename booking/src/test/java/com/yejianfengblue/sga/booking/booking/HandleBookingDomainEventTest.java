package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;
import static com.yejianfengblue.sga.booking.booking.BookingEngine.BOOKING_EVENT_OUT_BINDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {"spring.cloud.stream.source = booking"})
@Import(TestChannelBinderConfiguration.class)
@WithMockUser
public class HandleBookingDomainEventTest {

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenConfirmBooking_thenBookingEventIsSent() throws JsonProcessingException {

        Booking booking = new Booking(null,
                "SG", "001", LocalDate.of(2020, 1, 1),
                "HKG", "TPE", null, DRAFT,
                null, null, null, null);
        // when
        booking.confirm();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CONFIRMED);

        // then
        String bookingEventPayload = new String(outputDestination.receive(100, BOOKING_EVENT_OUT_BINDING).getPayload());
        JsonNode bookingEventJson = objectMapper.readTree(bookingEventPayload);
        assertThat(bookingEventJson.hasNonNull("id")).isTrue();
        assertThat(bookingEventJson.hasNonNull("timestamp")).isTrue();
        assertThat(bookingEventJson.get("type").asText()).isEqualTo(BookingEvent.Type.CONFIRM.toString());
        JsonNode bookingJson = bookingEventJson.get("booking");
        assertThat(bookingJson.get("carrier").asText()).isEqualTo(booking.getCarrier());
        assertThat(bookingJson.get("fltNum").asText()).isEqualTo(booking.getFltNum());
        assertThat(bookingJson.get("fltDate").asText()).isEqualTo(booking.getFltDate().toString());
        assertThat(bookingJson.get("segOrig").asText()).isEqualTo(booking.getSegOrig());
        assertThat(bookingJson.get("segDest").asText()).isEqualTo(booking.getSegDest());
        // TODO assert fare after handling fare
        assertThat(bookingJson.get("status").asText()).isEqualTo(booking.getStatus().toString());
        assertThat(bookingJson.get("createdBy").asText()).isEqualTo(booking.getCreatedBy());
        assertThat(bookingJson.get("createdDate").asText()).isEqualTo(booking.getCreatedDate().toString());
        assertThat(bookingJson.get("lastModifiedBy").asText()).isEqualTo(booking.getLastModifiedBy());
        assertThat(bookingJson.get("lastModifiedDate").asText()).isEqualTo(booking.getLastModifiedDate().toString());
    }

    @Test
    void whenCheckInBooking_thenBookingEventIsSent() throws JsonProcessingException {

        Booking booking = new Booking(null,
                "SG", "001", LocalDate.of(2020, 1, 1),
                "HKG", "TPE", null, CONFIRMED,
                null, null, null, null);
        // when
        booking.checkIn();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CHECKED_IN);

        // then
        String bookingEventPayload = new String(outputDestination.receive(100, BOOKING_EVENT_OUT_BINDING).getPayload());
        JsonNode bookingEventJson = objectMapper.readTree(bookingEventPayload);
        assertThat(bookingEventJson.hasNonNull("id")).isTrue();
        assertThat(bookingEventJson.hasNonNull("timestamp")).isTrue();
        assertThat(bookingEventJson.get("type").asText()).isEqualTo(BookingEvent.Type.CHECK_IN.toString());
        JsonNode bookingJson = bookingEventJson.get("booking");
        assertThat(bookingJson.get("carrier").asText()).isEqualTo(booking.getCarrier());
        assertThat(bookingJson.get("fltNum").asText()).isEqualTo(booking.getFltNum());
        assertThat(bookingJson.get("fltDate").asText()).isEqualTo(booking.getFltDate().toString());
        assertThat(bookingJson.get("segOrig").asText()).isEqualTo(booking.getSegOrig());
        assertThat(bookingJson.get("segDest").asText()).isEqualTo(booking.getSegDest());
        // TODO assert fare after handling fare
        assertThat(bookingJson.get("status").asText()).isEqualTo(booking.getStatus().toString());
        assertThat(bookingJson.get("createdBy").asText()).isEqualTo(booking.getCreatedBy());
        assertThat(bookingJson.get("createdDate").asText()).isEqualTo(booking.getCreatedDate().toString());
        assertThat(bookingJson.get("lastModifiedBy").asText()).isEqualTo(booking.getLastModifiedBy());
        assertThat(bookingJson.get("lastModifiedDate").asText()).isEqualTo(booking.getLastModifiedDate().toString());
    }

    @Test
    void whenCancelDraftBooking_thenBookingEventNotSent() {

        Booking booking = new Booking(null,
                "SG", "001", LocalDate.of(2020, 1, 1),
                "HKG", "TPE", null, DRAFT,
                null, null, null, null);
        // when
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);

        // then
        Message<byte[]> receivedMessage = outputDestination.receive(100, BOOKING_EVENT_OUT_BINDING);
        assertThat(receivedMessage).isNull();
    }

    @Test
    void whenCancelConfirmedBooking_thenBookingEventIsSent() throws JsonProcessingException {

        Booking booking = new Booking(null,
                "SG", "001", LocalDate.of(2020, 1, 1),
                "HKG", "TPE", null, CONFIRMED,
                null, null, null, null);
        // when
        booking.cancel();
        booking = bookingRepository.save(booking);
        assertThat(booking.getStatus()).isEqualTo(CANCELLED);

        // then
        String bookingEventPayload = new String(outputDestination.receive(100, BOOKING_EVENT_OUT_BINDING).getPayload());
        JsonNode bookingEventJson = objectMapper.readTree(bookingEventPayload);
        assertThat(bookingEventJson.hasNonNull("id")).isTrue();
        assertThat(bookingEventJson.hasNonNull("timestamp")).isTrue();
        assertThat(bookingEventJson.get("type").asText()).isEqualTo(BookingEvent.Type.CANCEL.toString());
        JsonNode bookingJson = bookingEventJson.get("booking");
        assertThat(bookingJson.get("carrier").asText()).isEqualTo(booking.getCarrier());
        assertThat(bookingJson.get("fltNum").asText()).isEqualTo(booking.getFltNum());
        assertThat(bookingJson.get("fltDate").asText()).isEqualTo(booking.getFltDate().toString());
        assertThat(bookingJson.get("segOrig").asText()).isEqualTo(booking.getSegOrig());
        assertThat(bookingJson.get("segDest").asText()).isEqualTo(booking.getSegDest());
        // TODO assert fare after handling fare
        assertThat(bookingJson.get("status").asText()).isEqualTo(booking.getStatus().toString());
        assertThat(bookingJson.get("createdBy").asText()).isEqualTo(booking.getCreatedBy());
        assertThat(bookingJson.get("createdDate").asText()).isEqualTo(booking.getCreatedDate().toString());
        assertThat(bookingJson.get("lastModifiedBy").asText()).isEqualTo(booking.getLastModifiedBy());
        assertThat(bookingJson.get("lastModifiedDate").asText()).isEqualTo(booking.getLastModifiedDate().toString());
    }
}
