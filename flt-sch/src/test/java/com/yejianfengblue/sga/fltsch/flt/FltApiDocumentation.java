package com.yejianfengblue.sga.fltsch.flt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@SpringBootTest
// the RestDocumentationExtension is auto configured with an output dir target/generated-snippets
@ExtendWith(RestDocumentationExtension.class)
public class FltApiDocumentation {

    private MockMvc mockMvc;

    @BeforeEach
    public void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

}
