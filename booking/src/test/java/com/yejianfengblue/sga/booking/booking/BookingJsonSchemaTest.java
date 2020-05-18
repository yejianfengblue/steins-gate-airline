package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.booking.util.JsonSchemaUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

@JsonTest
public class BookingJsonSchemaTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void assertBookingPropertyCouldSerialize() throws NoSuchFieldException {

        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("id")).isFalse();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("carrier")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("fltNum")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("fltDate")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("segOrig")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("segDest")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("fare")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("status")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("createdBy")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("createdDate")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("lastModifiedBy")).isTrue();
        JsonSchemaUtil.assertCouldSerialize(objectMapper, Booking.class.getDeclaredField("lastModifiedDate")).isTrue();
    }

    @Test
    void assertBookingPropertyCouldDeDeserialize() throws NoSuchFieldException {

        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("id")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("carrier")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("fltNum")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("fltDate")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("segOrig")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("segDest")).isTrue();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("fare")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("status")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("createdBy")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("createdDate")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("lastModifiedBy")).isFalse();
        JsonSchemaUtil.assertCouldDeserialize(objectMapper, Booking.class.getDeclaredField("lastModifiedDate")).isFalse();
    }
}
