package com.yejianfengblue.sga.booking;


import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class IndexApiDocumentation {

    private MockMvc mockMvc;

    @BeforeEach
    public void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    void index() throws Exception {

        this.mockMvc
                .perform(
                        get("/"))
                .andExpect(status().isOk())
                .andDo(document("index",
                        links(
                                linkWithRel("bookings").description("The <<resource_bookings, Bookings resource>>"),
                                linkWithRel("profile").description("The ALPS profile for this resource"))
                ));
    }

    @Test
    @SneakyThrows
    void genericErrorExample() {

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
                        MockMvcRequestBuilders.get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "No enough inventory")
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "http://localhost:8080/bookings/5ed50003c696b22bf07dc24d/confirm"))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("error", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("message", is("No enough inventory")))
                .andExpect(jsonPath("path", is("http://localhost:8080/bookings/5ed50003c696b22bf07dc24d/confirm")))
                .andDo(document("generic-error-example",
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
    void validationErrorExample() {

        mockMvc
                .perform(
                        post("/validation-error-example")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].entity").value("UserRegistration"))
                .andExpect(jsonPath("errors[0].property").value("password"))
                .andExpect(jsonPath("errors[0].invalidValue").value("1234"))
                .andExpect(jsonPath("errors[0].message").value("size must be between 8 and 32"))
                .andDo(document("validation-error-example",
                        responseFields(
                                fieldWithPath("errors").description("An array of error description about validation errors"),
                                fieldWithPath("errors[].entity").description("Error entity name"),
                                fieldWithPath("errors[].property").description("Error property name"),
                                fieldWithPath("errors[].invalidValue").description("Invalid value given in request"),
                                fieldWithPath("errors[].message").description("Validation error description"))));
    }

    @TestConfiguration
    @Import(ExampleController.class)
    static class TestConfig {

    }

    @RestController
    @Validated
    static class ExampleController {

        @PostMapping("/validation-error-example")
        ResponseEntity post() {

            ValidationErrorsMessage message = new ValidationErrorsMessage();
            message.add(ValidationError.of("UserRegistration", "password", "1234", "size must be between 8 and 32"));
            return ResponseEntity.badRequest().body(message);
        }
    }

    /**
     * Mimic {@link RepositoryConstraintViolationExceptionMessage}, a wrapper of {@code List<ValidationError>}
     */
    @Getter
    static class ValidationErrorsMessage {

        private final List<ValidationError> errors = new ArrayList<>();

        void add(ValidationError validationError) {
            errors.add(validationError);
        }
    }

}
