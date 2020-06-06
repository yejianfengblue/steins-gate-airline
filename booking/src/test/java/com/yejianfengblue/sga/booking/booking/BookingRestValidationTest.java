package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class BookingRestValidationTest {

    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookingRepository bookingRepository;

    @BeforeEach
    void configMockMvc(WebApplicationContext webAppContext) {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .build();
    }

    @AfterEach
    void cleanTestData() {
        bookingRepository.deleteAll();
    }

    @Test
    void createBookingSuccess() throws Exception {

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isCreated());
    }

    @Test
    void carrierNotNull() throws Exception {

        String property = "carrier";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.remove(property);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        bookingPostRequestBody.put(property, null);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void carrierSizeIs2() throws Exception {

        String property = "carrier";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.put(property, "SG");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isCreated());

        bookingPostRequestBody.put(property, "S");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("S"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 2, 2)));

        bookingPostRequestBody.put(property, "SGS");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("SGS"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 2, 2)));
    }

    @Test
    void fltNumNotNull() throws Exception {

        String property = "fltNum";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.remove(property);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        bookingPostRequestBody.put(property, null);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void fltNumSizeBetween3And5() throws Exception {

        String property = "fltNum";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.put(property, "123");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isCreated());

        bookingPostRequestBody.put(property, "12");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("12"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 5)));

        bookingPostRequestBody.put(property, "123456");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("123456"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 5)));
    }

    @Test
    void fltDateNotNull() throws Exception {

        String property = "fltDate";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.remove(property);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        bookingPostRequestBody.put(property, null);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void segOrigNotNull() throws Exception {

        String property = "segOrig";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.remove(property);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        bookingPostRequestBody.put(property, null);
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void segOrigSizeIs3() throws Exception {

        String property = "segOrig";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.put(property, "HKG");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isCreated());

        bookingPostRequestBody.put(property, "HK");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HK"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));


        bookingPostRequestBody.put(property, "HKG1");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HKG1"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));
    }

    @Test
    void segDestSizeIs3() throws Exception {

        String property = "segDest";

        HashMap<String, Object> bookingPostRequestBody = validBooking();

        bookingPostRequestBody.put(property, "HKG");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isCreated());

        bookingPostRequestBody.put(property, "HK");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HK"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));


        bookingPostRequestBody.put(property, "HKG1");
        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPostRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HKG1"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));
    }

    private static HashMap<String, Object> validBooking() {

        HashMap<String, Object> booking = new HashMap<>();
        booking.put("carrier", "SG");
        booking.put("fltNum", "001");
        booking.put("fltDate", "2020-01-01");
        booking.put("segOrig", "HKG");
        booking.put("segDest", "TPE");
        booking.put("passenger", "Tester");

        return booking;
    }

    private final static String NOT_NULL_MESSAGE = "must not be null";

    private final static String SIZE_MESSAGE = "size must be between %d and %d";

}
