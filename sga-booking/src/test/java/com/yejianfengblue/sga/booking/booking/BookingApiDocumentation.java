package com.yejianfengblue.sga.booking.booking;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.booking.common.ServiceType;
import com.yejianfengblue.sga.booking.inventory.Inventory;
import com.yejianfengblue.sga.booking.inventory.InventoryLeg;
import com.yejianfengblue.sga.booking.inventory.InventoryService;
import com.yejianfengblue.sga.booking.util.RestdocsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@WithMockUser
@Slf4j
public class BookingApiDocumentation {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private LinkDiscoverer linkDiscoverer;

    private static final String bookingBasePath = "/bookings";

    @MockBean
    private InventoryService inventoryService;

    @BeforeEach
    public void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @AfterEach
    void deleteTestData() {
        bookingRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void accessBookingsTest() {

        // prepared test data
        Booking sg001 = bookingRepository.save(
                new Booking("SG", "001", LocalDate.of(2020, 1, 1), "HKG", "TPE", "Tester"));
        Booking sg002 = bookingRepository.save(
                new Booking("SG", "001", LocalDate.of(2020, 1, 2), "HKG", "TPE", "Tester"));

        MockHttpServletResponse response = accessBookingsResource();
    }

    @Test
    void createBookingTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(
                        new Inventory("SG", "001", ServiceType.PAX,
                                fltDate, fltDate.getDayOfWeek().getValue(),
                                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                        legDep, legArr, 1,
                                        fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                        1)))));

        createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
    }

    @Test
    @SneakyThrows
    void createBookingValidationErrorTest() {

        this.mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(Map.of(
                                        "carrier", "SG", "fltNum", "001",
                                        "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].property").value("fltDate"))
                .andExpect(jsonPath("errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("errors[0].message").value("must not be null"))
                .andDo(document("bookings-create-validation-error",
                        responseFields(
                                fieldWithPath("errors").description("An array of error description about validation errors"),
                                fieldWithPath("errors[].entity").description("Error entity name"),
                                fieldWithPath("errors[].property").description("Error property name"),
                                fieldWithPath("errors[].invalidValue").description("Invalid value given in request"),
                                fieldWithPath("errors[].message").description("Validation error description"))));
    }

    @Test
    void getBookingTest() {

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        String bookingLocation = response.getHeader(LOCATION);

        getBooking(bookingLocation);
    }

    @Test
    void updateBookingTest() {

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        String bookingLocation = response.getHeader(LOCATION);

        updateBooking(bookingLocation, Map.of("fltDate", "2020-01-02"));
    }

    @Test
    @SneakyThrows
    void updateBookingValidationErrorTest() {

        // create
        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        String bookingLocation = response.getHeader(LOCATION);

        // update
        Map<String, String> bookingPropertiesToUpdate = Map.of(
                "carrier", "SGS");
        this.mockMvc
                .perform(
                        patch(bookingLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsBytes(bookingPropertiesToUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].property").value("carrier"))
                .andExpect(jsonPath("errors[0].invalidValue").value("SGS"))
                .andExpect(jsonPath("errors[0].message").value("size must be between 2 and 2"))
                .andDo(document("bookings-update-validation-error",
                        responseFields(
                                fieldWithPath("errors").description("An array of error description about validation errors"),
                                fieldWithPath("errors[].entity").description("Error entity name"),
                                fieldWithPath("errors[].property").description("Error property name"),
                                fieldWithPath("errors[].invalidValue").description("Invalid value given in request"),
                                fieldWithPath("errors[].message").description("Validation error description"))));
    }

    @Test
    void confirmBookingTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(
                        new Inventory("SG", "001", ServiceType.PAX,
                                fltDate, fltDate.getDayOfWeek().getValue(),
                                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                        legDep, legArr, 1,
                                        fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                        1)))));

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));

        confirmBooking(response);
    }

    @Test
    @SneakyThrows
    void confirmBookingNoEnoughInventoryTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        // inventory available is 0
        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(
                        new Inventory("SG", "001", ServiceType.PAX,
                                fltDate, fltDate.getDayOfWeek().getValue(),
                                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                        legDep, legArr, 1,
                                        fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                        0)))));

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));

        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("confirm"), response.getContentAsString());

        response = mockMvc
                .perform(
                        put(confirmLink.getHref()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        /* When a exception (e.g., ResponseStatusException) is thrown from MVC layer (e.g., Controller), an error response is being returned.
         * The container forwards the request to the error page which causes Spring Boot's `BasicErrorController`
         * to be driven. `BasicErrorController` produces the JSON output describing the failure.
         *
         * Due to the limitation of Spring MVC Test, it's unaware of error pages (and also doesn't fully support
         * the forwarding of requests) so we're left with a "plain" error response (MockHttpServletResponse)
         * with attributes such as `errorMessage` but empty body.
         *
         * To mimic the forwarding of an error, directly call Spring Boot's error controller `/error` and configure
         * the necessary request attributes.
         *
         * Alternative solutions:
         * 1. TestRestTemplate: an integration test using `@SpringBootTest` configured with a `DEFINED_PORT` or
         * `RANDOM_PORT` web environment and `TestRestTemplate`, so the error response is precisely rendered.
         * Unfortunately `TestRestTemplate` doesn't work well with `Spring REST Docs`.
         *
         * 2. WebTestClient: it works in Webflux application and works well with `Spring REST Docs`.
         *
         * Reference:
         * https://github.com/spring-projects/spring-restdocs/issues/23#issuecomment-75523788
         * https://github.com/spring-projects/spring-boot/issues/7321#issuecomment-261343803
         * https://github.com/spring-projects/spring-boot/issues/7321#issuecomment-348013162
         */
        mockMvc
                .perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, response.getErrorMessage())
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, confirmLink.getHref()))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("error", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("message", is("No enough inventory")))
                .andExpect(jsonPath("path", is(confirmLink.getHref())))
                .andDo(document("booking-confirm-no-enough-inventory",
                        responseFields(
                                fieldWithPath("timestamp").description("Error time in milliseconds in UTC time zone"),
                                fieldWithPath("status").description("HTTP status code"),
                                fieldWithPath("error").description("HTTP error"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made")
                        )));
    }

    @Test
    @SneakyThrows
    void confirmBookingInventoryNotFoundTest() {

        // inventory doesn't exist
        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.empty());

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));

        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("confirm"), response.getContentAsString());

        response = mockMvc
                .perform(
                        put(confirmLink.getHref()))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();

        mockMvc
                .perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, response.getErrorMessage())
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, confirmLink.getHref()))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("error", is(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())))
                .andExpect(jsonPath("message", is("Inventory not found")))
                .andExpect(jsonPath("path", is(confirmLink.getHref())))
                .andDo(document("booking-confirm-inventory-not-found",
                        responseFields(
                                fieldWithPath("timestamp").description("Error time in milliseconds in UTC time zone"),
                                fieldWithPath("status").description("HTTP status code"),
                                fieldWithPath("error").description("HTTP error"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made")
                        )));
    }

    @Test
    @SneakyThrows
    void confirmBookingInvalidStatusTransitionTest() {

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("confirm"), response.getContentAsString());
        response = cancelBooking(response);

        // it's valid to confirm cancelled booking
        response = mockMvc
                .perform(
                        put(confirmLink.getHref()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        mockMvc
                .perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, response.getErrorMessage())
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, confirmLink.getHref()))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("error", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("message", is("Booking status cannot be transited from CANCELLED to CONFIRMED")))
                .andExpect(jsonPath("path", is(confirmLink.getHref())))
                .andDo(document("booking-confirm-invalid-status-transition",
                        responseFields(
                                fieldWithPath("timestamp").description("Error time in milliseconds in UTC time zone"),
                                fieldWithPath("status").description("HTTP status code"),
                                fieldWithPath("error").description("HTTP error"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made")
                        )));
    }

    @Test
    void checkInBookingTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(new Inventory(
                        "SG", "001", ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                legDep, legArr, 1,
                                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                1)))));

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));

        response = confirmBooking(response);

        checkInBooking(response);
    }

    @Test
    @SneakyThrows
    void checkInBookingInvalidStatusTransitionTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(new Inventory(
                        "SG", "001", ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                legDep, legArr, 1,
                                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                1)))));

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        response = confirmBooking(response);
        Link checkInLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("check-in"), response.getContentAsString());
        response = cancelBooking(response);

        // it's valid to check-in cancelled booking
        response = mockMvc
                .perform(
                        put(checkInLink.getHref()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        mockMvc
                .perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, response.getErrorMessage())
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, checkInLink.getHref()))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("error", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("message", is("Booking status cannot be transited from CANCELLED to CHECKED_IN")))
                .andExpect(jsonPath("path", is(checkInLink.getHref())))
                .andDo(document("booking-check-in-invalid-status-transition",
                        responseFields(
                                fieldWithPath("timestamp").description("Error time in milliseconds in UTC time zone"),
                                fieldWithPath("status").description("HTTP status code"),
                                fieldWithPath("error").description("HTTP error"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made")
                        )));
    }

    @Test
    void cancelBookingTest() {

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));

        cancelBooking(response);
    }

    @Test
    @SneakyThrows
    void cancelBookingInvalidStatusTransitionTest() {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        given(this.inventoryService.findInventory("SG", "001", LocalDate.of(2020, 1, 1)))
                .willReturn(Optional.of(new Inventory(
                        "SG", "001", ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                legDep, legArr, 1,
                                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                1)))));

        MockHttpServletResponse response = createNewBooking(Map.of(
                "carrier", "SG", "fltNum", "001", "fltDate", "2020-01-01",
                "segOrig", "HKG", "segDest", "TPE", "passenger", "Tester"));
        Link cancelLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("cancel"), response.getContentAsString());
        response = confirmBooking(response);
        response = checkInBooking(response);

        // it's valid to cancel checked-in booking
        response = mockMvc
                .perform(
                        delete(cancelLink.getHref()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        mockMvc
                .perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, response.getErrorMessage())
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, cancelLink.getHref()))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("error", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("message", is("Booking status cannot be transited from CHECKED_IN to CANCELLED")))
                .andExpect(jsonPath("path", is(cancelLink.getHref())))
                .andDo(document("booking-cancel-invalid-status-transition",
                        responseFields(
                                fieldWithPath("timestamp").description("Error time in milliseconds in UTC time zone"),
                                fieldWithPath("status").description("HTTP status code"),
                                fieldWithPath("error").description("HTTP error"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made")
                        )));
    }

    @SneakyThrows
    private MockHttpServletResponse accessBookingsResource() {

        MockHttpServletResponse response = mockMvc
                .perform(
                        get(bookingBasePath)
                                .accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("bookings-get",
                        RestdocsUtil.links(
                                linkWithRel("profile").description("The ALPS profile for this resource")),
                        RestdocsUtil.responseFields(
                                subsectionWithPath("_embedded.bookings").description("An array of <<resource_booking, Booking resource>>"),
                                subsectionWithPath("page").description("Pagination parameter")
                        )
                ))
                .andReturn().getResponse();

        return response;
    }

    @SneakyThrows
    private MockHttpServletResponse createNewBooking(Map<String, Object> booking) {

        RestdocsUtil.ConstrainedFields bookingConstrainedFields = new RestdocsUtil.ConstrainedFields(Booking.class);

        ResultActions resultActions = mockMvc
                .perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking))
                                .accept(MediaTypes.HAL_JSON));

        MockHttpServletResponse response = resultActions.andReturn().getResponse();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andExpect(jsonPath("carrier").value(booking.get("carrier")))
                .andExpect(jsonPath("fltNum").value(booking.get("fltNum")))
                .andExpect(jsonPath("fltDate").value(booking.get("fltDate")))
                .andExpect(jsonPath("segOrig").value(booking.get("segOrig")))
                .andExpect(jsonPath("segDest").value(booking.get("segDest")))
                .andExpect(jsonPath("passenger").value(booking.get("passenger")))
                .andExpect(jsonPath("fare").exists())
                .andExpect(jsonPath("fare.amount").exists())
                .andExpect(jsonPath("fare.currency").exists())
                .andExpect(jsonPath("status").value(DRAFT.toString()))
                .andExpect(jsonPath("_links.self.href").value(response.getHeader(LOCATION)))
                .andExpect(jsonPath("_links.booking.href").value(response.getHeader(LOCATION)))
                .andExpect(jsonPath("_links.confirm.href").value(response.getHeader(LOCATION) + "/confirm"))
                .andExpect(jsonPath("_links.cancel.href").value(response.getHeader(LOCATION) + "/cancel"))
                .andDo(document("bookings-create",
                        requestFields(
                                bookingConstrainedFields.withPath("carrier").description("Flight carrier"),
                                bookingConstrainedFields.withPath("fltNum").description("Flight number"),
                                bookingConstrainedFields.withPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                bookingConstrainedFields.withPath("segOrig").description("Flight segment origin airport"),
                                bookingConstrainedFields.withPath("segDest").description("Flight segment destination airport"),
                                bookingConstrainedFields.withPath("passenger").description("Passenger name")),
                        responseHeaders(
                                headerWithName(LOCATION).description("Created booking resource location")),
                        RestdocsUtil.responseFields(
                                fieldWithPath("carrier").description("Flight carrier"),
                                fieldWithPath("fltNum").description("Flight number"),
                                fieldWithPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                fieldWithPath("segOrig").description("Flight segment origin airport"),
                                fieldWithPath("segDest").description("Flight segment destination airport"),
                                fieldWithPath("passenger").description("Passenger name"),
                                fieldWithPath("fare").description("Booking fare"),
                                fieldWithPath("fare.amount").description("Booking fare amount"),
                                fieldWithPath("fare.currency").description("Booking fare currency"),
                                fieldWithPath("status").description("Booking status"))
                                .and(RestdocsUtil.AUDIT_FIELDS),
                        RestdocsUtil.links(
                                linkWithRel("booking").description("The booking itself"),
                                linkWithRel("confirm").description("Confirm this booking"),
                                linkWithRel("cancel").description("Cancel this booking"))
                ));

        return response;
    }

    @SneakyThrows
    private MockHttpServletResponse updateBooking(String bookingLocation, Map<String, Object> bookingPropertiesToUpdate) {

        RestdocsUtil.ConstrainedFields bookingConstrainedFields = new RestdocsUtil.ConstrainedFields(Booking.class);

        MockHttpServletResponse response = this.mockMvc
                .perform(
                        patch(bookingLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(bookingPropertiesToUpdate)))
                .andExpect(status().isNoContent())
                .andDo(document("booking-update",
                        requestFields(
                                bookingConstrainedFields.withPath("carrier").type(JsonFieldType.STRING).optional().description("Flight carrier"),
                                bookingConstrainedFields.withPath("fltNum").type(JsonFieldType.STRING).optional().description("Flight number"),
                                bookingConstrainedFields.withPath("fltDate").type(JsonFieldType.STRING).optional().description("Flight date in format yyyy-MM-dd"),
                                bookingConstrainedFields.withPath("segOrig").type(JsonFieldType.STRING).optional().description("Flight segment origin airport"),
                                bookingConstrainedFields.withPath("segDest").type(JsonFieldType.STRING).optional().description("Flight segment destination airport")
                        )
                ))
                .andReturn().getResponse();

        // verify
        ResultActions resultActions = mockMvc
                .perform(
                        get(bookingLocation)
                                .accept(MediaTypes.HAL_JSON));
        for (Map.Entry<String, Object> entry : bookingPropertiesToUpdate.entrySet()) {
            resultActions.andExpect(jsonPath(entry.getKey()).value(entry.getValue()));
        }

        return response;

    }

    @SneakyThrows
    private MockHttpServletResponse getBooking(String bookingLocation) {

        MockHttpServletResponse response = this.mockMvc
                .perform(
                        get(bookingLocation)
                                .accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("booking-get",
                        RestdocsUtil.responseFields(
                                fieldWithPath("carrier").description("Flight carrier"),
                                fieldWithPath("fltNum").description("Flight number"),
                                fieldWithPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                fieldWithPath("segOrig").description("Flight segment origin airport"),
                                fieldWithPath("segDest").description("Flight segment destination airport"),
                                fieldWithPath("passenger").description("Passenger name"),
                                fieldWithPath("fare").description("Booking fare"),
                                fieldWithPath("fare.amount").description("Booking fare amount"),
                                fieldWithPath("fare.currency").description("Booking fare currency"),
                                fieldWithPath("status").description("Booking status")
                        ).and(RestdocsUtil.AUDIT_FIELDS),
                        RestdocsUtil.links(
                                linkWithRel("booking").description("The booking itself"),
                                linkWithRel("confirm").optional().description("Confirm this booking"),
                                linkWithRel("check-in").optional().description("Check-in this booking"),
                                linkWithRel("cancel").optional().description("Cancel this booking"))
                ))
                .andReturn().getResponse();

        return response;
    }

    @SneakyThrows
    private MockHttpServletResponse confirmBooking(MockHttpServletResponse sourceResponse) {

        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("confirm"), sourceResponse.getContentAsString());

        MockHttpServletResponse resultResponse = mockMvc
                .perform(
                        put(confirmLink.getHref()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(CONFIRMED.toString()))
                .andDo(document("booking-confirm"))
                .andReturn().getResponse();

        return resultResponse;
    }

    @SneakyThrows
    private MockHttpServletResponse checkInBooking(MockHttpServletResponse sourceResponse) {

        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("check-in"), sourceResponse.getContentAsString());

        MockHttpServletResponse resultResponse = mockMvc
                .perform(
                        put(confirmLink.getHref()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(CHECKED_IN.toString()))
                .andDo(document("booking-check-in"))
                .andReturn().getResponse();

        return resultResponse;
    }

    @SneakyThrows
    private MockHttpServletResponse cancelBooking(MockHttpServletResponse sourceResponse) {

        Link confirmLink = linkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("cancel"), sourceResponse.getContentAsString());

        MockHttpServletResponse resultResponse = mockMvc
                .perform(
                        delete(confirmLink.getHref()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(CANCELLED.toString()))
                .andDo(document("booking-cancel"))
                .andReturn().getResponse();

        return resultResponse;
    }

}
