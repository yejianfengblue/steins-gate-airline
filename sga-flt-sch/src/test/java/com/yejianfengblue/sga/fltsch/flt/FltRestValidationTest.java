package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ensure the bean validation works when create flt via HTTP POST request and update flt via HTTP PATCH request
 */
@SpringBootTest
@WithMockUser(roles = "flt-sch-user")
@Slf4j
public class FltRestValidationTest {

    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private FltRepository fltRepository;

    @BeforeEach
    void configMockMvc(WebApplicationContext webAppContext) {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .build();
    }

    @AfterEach
    void deleteTestData() {
        this.fltRepository.deleteAll();
    }

    @Test
    void createFltSuccess() throws Exception {

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated());
    }

    @Test
    void carrierNotNull() throws Exception {

        String property = "carrier";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.remove(property);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        // even though add a key "carrier" with value null, still fail
        fltPostRequestPayload.put(property, null);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void carrierSizeIs2() throws Exception {

        String property = "carrier";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.put(property, "S");

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("S"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 2, 2)));

        fltPostRequestPayload.put(property, "SGS");
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("SGS"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 2, 2)));
    }

    @Test
    void fltNumNotNull() throws Exception {

        String property = "fltNum";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.remove(property);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltPostRequestPayload.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void fltNumSizeBetween3And5() throws Exception {

        String property = "fltNum";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.put(property, "12");

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("12"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 5)));

        fltPostRequestPayload.put(property, "123456");
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("123456"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 5)));
    }

    @Test
    void fltDateNotNull() throws Exception {

        String property = "fltDate";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.remove(property);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltPostRequestPayload.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void fltDowNotNull() throws Exception {

        String property = "fltDow";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.remove(property);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltPostRequestPayload.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void fltDowBetween1And7() throws Exception {

        String property = "fltDow";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.put(property, 0);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(0))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MIN_MESSAGE, 1)));

        fltPostRequestPayload.put(property, 8);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(8))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MAX_MESSAGE, 7)));
    }

    @Test
    void fltLegsNotEmpty() throws Exception {

        String property = "fltLegs";

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();
        fltPostRequestPayload.remove(property);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(either(empty()).or(nullValue())))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_EMPTY_MESSAGE));

        fltPostRequestPayload.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(either(empty()).or(nullValue())))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_EMPTY_MESSAGE));

        fltPostRequestPayload.put(property, new ArrayList<>());
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(either(empty()).or(nullValue())))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_EMPTY_MESSAGE));
    }

    //////////////////////////////////// fltLegs property START

    @Test
    void depDateNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depDate";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void depDowNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depDow";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void depDowBetween1And7() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depDow";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, 0);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(0))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MIN_MESSAGE, 1)));

        fltLeg.put(property, 8);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(8))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MAX_MESSAGE, 7)));
    }

    @Test
    void legDepNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legDep";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void legDepSizeIs3() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legDep";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, "HK");
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HK"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));

        fltLeg.put(property, "HKG1");
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HKG1"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));
    }

    @Test
    void legArrNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legArr";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void legArrSizeIs3() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legArr";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, "HK");
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HK"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));

        fltLeg.put(property, "HKG1");
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("HKG1"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));
    }

    @Test
    void legSeqNumNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legSeqNum";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void legSeqNumMin1() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "legSeqNum";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, 0);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(0))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MIN_MESSAGE, 1)));
    }

    @Test
    void schDepTimeNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "schDepTime";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void schArrTimeNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "schArrTime";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void depTimeDiffNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depTimeDiff";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void depTimeDiffBetweenMinus720And840() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depTimeDiff";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, -721);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(-721))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MIN_MESSAGE, -720)));

        fltLeg.put(property, 841);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(841))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MAX_MESSAGE, 840)));
    }

    @Test
    void arrTimeDiffNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "arrTimeDiff";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void arrTimeDiffBetweenMinus720And840() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "arrTimeDiff";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, -721);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(-721))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MIN_MESSAGE, -720)));

        fltLeg.put(property, 841);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(841))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(MAX_MESSAGE, 840)));
    }

    @Test
    void iataAcTypeNotNull() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "iataAcType";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.remove(property);
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));

        fltLeg.put(property, null);
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    @Test
    void iataAcTypeSizeIs3() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "iataAcType";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put(property, "12");
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("12"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));

        fltLeg.put(property, "1234");
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value("1234"))
                .andExpect(jsonPath("$.errors[0].message").value(String.format(SIZE_MESSAGE, 3, 3)));
    }

    //////////////////////////////////// fltLegs property END

    // One positive and negative test to prove that bean validation also works via PATCH request

    @Test
    void patchUpdateFltSuccess() throws Exception {

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put("iataAcType", "333");
        fltLeg.put("acReg", "B-LAD");
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        // create a flt with a leg whose IATA AC type is 333
        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // get the created flt
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs[0].iataAcType").value("333"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"));

        // PATCH update
        fltLeg.put("iataAcType", "773");
        fltLeg.put("acReg", "B-HNK");
        HashMap<String, Object> fltPatchRequestPayload = validFlt(List.of(fltLeg));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload)))
                .andExpect(status().isNoContent());

        // get the updated flt
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs[0].iataAcType").value("773"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-HNK"));
    }

    @Test
    void patchUpdateFltValidationError() throws Exception {

        String propertyPrefix = "fltLegs[0].";
        String property = "depDate";

        HashMap<String, Object> fltLeg = validFltLeg();
        fltLeg.put("iataAcType", "333");
        fltLeg.put("acReg", "B-LAD");
        HashMap<String, Object> fltPostRequestPayload = validFlt(List.of(fltLeg));

        // create a flt with a leg whose IATA AC type is 333
        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // get the created flt
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs[0].iataAcType").value("333"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"));

        // PATCH update
        HashMap<String, Object> fltLegToUpdate = new HashMap<>();
        fltLegToUpdate.put("depDate", null);
        fltLegToUpdate.put("depDow", 3);
        fltLegToUpdate.put("legDep", "HKG");
        fltLegToUpdate.put("legArr", "TPE");
        fltLegToUpdate.put("legSeqNum", 1);
        fltLegToUpdate.put("schDepTime", "2020-01-01T00:00:00");
        fltLegToUpdate.put("schArrTime", "2020-01-01T04:00:00");
        fltLegToUpdate.put("estDepTime", "2020-01-01T00:00:00");
        fltLegToUpdate.put("estArrTime", "2020-01-01T04:00:00");
        fltLegToUpdate.put("actDepTime", "2020-01-01T00:00:00");
        fltLegToUpdate.put("actArrTime", "2020-01-01T04:00:00");
        fltLegToUpdate.put("depTimeDiff", 480);
        fltLegToUpdate.put("arrTimeDiff", 480);
        fltLegToUpdate.put("acReg", "B-LAD");
        fltLegToUpdate.put("iataAcType", "773");

        HashMap<String, Object> fltPatchRequestPayload = validFlt(List.of(fltLegToUpdate));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property").value(propertyPrefix + property))
                .andExpect(jsonPath("$.errors[0].invalidValue").value(nullValue()))
                .andExpect(jsonPath("$.errors[0].message").value(NOT_NULL_MESSAGE));
    }

    private static HashMap<String, Object> validFlt(List<HashMap<String, Object>> fltLegs) {

        HashMap<String, Object> flt = new HashMap<>();
        flt.put("carrier", "SG");
        flt.put("fltNum", "001");
        flt.put("serviceType", "PAX");
        flt.put("fltDate", "2020-01-01");
        flt.put("fltDow", 3);
        flt.put("fltLegs", fltLegs);

        return flt;
    }

    private static HashMap<String, Object> validFltWith1Leg() {

        HashMap<String, Object> flt = new HashMap<>();
        flt.put("carrier", "SG");
        flt.put("fltNum", "001");
        flt.put("serviceType", "PAX");
        flt.put("fltDate", "2020-01-01");
        flt.put("fltDow", 3);
        flt.put("fltLegs", List.of(validFltLeg()));

        return flt;
    }

    private static HashMap<String, Object> validFltLeg() {

        HashMap<String, Object> fltLeg = new HashMap<>();
        fltLeg.put("depDate", "2020-01-01");
        fltLeg.put("depDow", 3);
        fltLeg.put("legDep", "HKG");
        fltLeg.put("legArr", "TPE");
        fltLeg.put("legSeqNum", 1);
        fltLeg.put("schDepTime", "2020-01-01T00:00:00");
        fltLeg.put("schArrTime", "2020-01-01T04:00:00");
        fltLeg.put("estDepTime", "2020-01-01T00:00:00");
        fltLeg.put("estArrTime", "2020-01-01T04:00:00");
        fltLeg.put("actDepTime", "2020-01-01T00:00:00");
        fltLeg.put("actArrTime", "2020-01-01T04:00:00");
        fltLeg.put("depTimeDiff", 480);
        fltLeg.put("arrTimeDiff", 480);
        fltLeg.put("acReg", "B-LAD");
        fltLeg.put("iataAcType", "333");

        return fltLeg;
    }

    private final static String NOT_NULL_MESSAGE = "must not be null";

    private final static String NOT_EMPTY_MESSAGE = "must not be empty";

    private final static String SIZE_MESSAGE = "size must be between %d and %d";

    private final static String MIN_MESSAGE = "must be greater than or equal to %d";

    private final static String MAX_MESSAGE = "must be less than or equal to %d";
}
