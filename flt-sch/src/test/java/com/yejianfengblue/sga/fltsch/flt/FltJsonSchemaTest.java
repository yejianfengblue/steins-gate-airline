package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.fltsch.util.JsonSchemaUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest
@Slf4j
public class FltJsonSchemaTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void assertFltPropertyCouldSerialize() throws NoSuchFieldException {

        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("carrier")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("fltNum")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("serviceType")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("fltDate")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("fltDow")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("fltLegs")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("createdBy")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("createdDate")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("lastModifiedBy")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Flt.class.getDeclaredField("lastModifiedDate")).isTrue();
    }

    @Test
    void assertFltPropertyCouldDeserialize() throws NoSuchFieldException {

        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("carrier")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("fltNum")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("serviceType")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("fltDate")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("fltDow")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("fltLegs")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("createdBy")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("createdDate")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("lastModifiedBy")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Flt.class.getDeclaredField("lastModifiedDate")).isFalse();
    }
}
