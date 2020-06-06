package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class FltRestSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private JwtRequestPostProcessor everyoneJwt;

    private JwtRequestPostProcessor fltSchUserJwt;

    @Autowired
    private FltRepository fltRepository;

    @BeforeEach
    void configMockMvc(WebApplicationContext webAppContext) {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setupJwt() {

        everyoneJwt = jwt()
                .authorities(jwtGrantedAuthoritiesConverter)
                .jwt(Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .claim("sub", "user")
                        .claim("scope", "openid")
                        .claim("groups", Set.of("Everyone"))
                        .build());

        fltSchUserJwt = jwt()
                .authorities(jwtGrantedAuthoritiesConverter)
                .jwt(Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .claim("sub", "user")
                        .claim("scope", "openid")
                        .claim("groups", Set.of("Everyone", "flt-sch-user"))
                        .build());
    }

    @AfterEach
    void deleteTestData() {
        this.fltRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void givenRequestWithoutJwt_whenGetFlts_thenUnauthorized() {

        this.mockMvc
                .perform(
                        get("/flts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void givenRequestWithJwtClaimGroupsEveryone_whenGetFlts_thenOk() {

        this.mockMvc
                .perform(
                        get("/flts")
                                .with(everyoneJwt))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void givenJwtWithClaimGroupsFltSchUser_whenCreateFlt_thenSuccessful() {

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
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
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(fltSchUserJwt))
                .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void givenJwtWithClaimGroupsFltSchUser_whenUpdateFlt_thenSuccessful() {

        // create
        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
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
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        String fltLocation = mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(fltSchUserJwt))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // update
        HashMap<String, Object> updatedFltLeg = new HashMap<>();
        updatedFltLeg.put("depDate", "2020-01-01");
        updatedFltLeg.put("depDow", 3);
        updatedFltLeg.put("legDep", "HKG");
        updatedFltLeg.put("legArr", "TPE");
        updatedFltLeg.put("legSeqNum", 1);
        updatedFltLeg.put("schDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("schArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("estDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("estArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("actDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("actArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("depTimeDiff", 480);
        updatedFltLeg.put("arrTimeDiff", 480);
        updatedFltLeg.put("acReg", "B-HNK");
        updatedFltLeg.put("iataAcType", "773");
        HashMap<String, Object> updateRequestPayload = new HashMap<>();
        updateRequestPayload.put("fltLegs", List.of(updatedFltLeg));

        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestPayload))
                                .with(fltSchUserJwt))
                .andExpect(status().is2xxSuccessful());

        // verify
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestPayload))
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs[0].acReg").value(updatedFltLeg.get("acReg")))
                .andExpect(jsonPath("fltLegs[0].iataAcType").value(updatedFltLeg.get("iataAcType")));
    }

    @Test
    @SneakyThrows
    void givenJwtWithClaimGroupsEveryone_whenCreateFlt_whenForbidden() {

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
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
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(everyoneJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void givenJwtWithClaimGroupsEveryone_whenUpdateFlt_whenForbidden() {

        // create
        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
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
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        String fltLocation = mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(fltSchUserJwt))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // update
        HashMap<String, Object> updatedFltLeg = new HashMap<>();
        updatedFltLeg.put("depDate", "2020-01-01");
        updatedFltLeg.put("depDow", 3);
        updatedFltLeg.put("legDep", "HKG");
        updatedFltLeg.put("legArr", "TPE");
        updatedFltLeg.put("legSeqNum", 1);
        updatedFltLeg.put("schDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("schArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("estDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("estArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("actDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("actArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("depTimeDiff", 480);
        updatedFltLeg.put("arrTimeDiff", 480);
        updatedFltLeg.put("acReg", "B-HNK");
        updatedFltLeg.put("iataAcType", "773");
        HashMap<String, Object> updateRequestPayload = new HashMap<>();
        updateRequestPayload.put("fltLegs", List.of(updatedFltLeg));

        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestPayload))
                                .with(everyoneJwt))
                .andExpect(status().isForbidden());

    }
}
