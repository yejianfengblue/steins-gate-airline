package com.yejianfengblue.sga.fltsch;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.fltsch.flt.FltRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class IndexApiDocumentation {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FltRepository fltRepository;

    @BeforeEach
    public void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void index() throws Exception {

        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andDo(document("index",
                        links(
                                linkWithRel("flts").description("The <<resources_flts, Flights resource>>"),
                                linkWithRel("profile").description("The ALPS profile for this resource")),
                        responseFields(
                                subsectionWithPath("_links").description("<<resources_index_access_links, Links>> to other resources"))
                ));
    }

}
