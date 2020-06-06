package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.AbstractJavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.flt.kafka.enabled = true"
})
@EmbeddedKafka(topics = "flt")
@Slf4j
public class FltKafkaTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate kafkaTemplate;

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
    @SneakyThrows
    void whenPostCreateFlt_thenCreateTypeFltEventIsSentToKafka() {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("createTestGroup", "true", this.embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "flt");

        // no message before POST
        ConsumerRecords<String, String> beforeCreateRecords = KafkaTestUtils.getRecords(consumer, 100);
        assertThat(beforeCreateRecords.count()).isZero();

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
        // POST
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated());

        // one FltEvent message is sent to Kafka
        ConsumerRecords<String, String> afterCreateRecords = KafkaTestUtils.getRecords(consumer, 100);
        assertThat(afterCreateRecords.count()).isOne();
        // verify the FltEvent message header and content
        ConsumerRecord<String, String> record = afterCreateRecords.iterator().next();
        log.info(record.toString());
        assertThat(new String(record.headers().lastHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME).value(), StandardCharsets.UTF_8))
                .isEqualTo("fltEvent");
        JsonNode fltEventJsonNode = objectMapper.readTree(record.value());
        assertThat(fltEventJsonNode.has("id")).isTrue();
        assertThat(fltEventJsonNode.has("timestamp")).isTrue();
        assertThat(fltEventJsonNode.get("type").textValue()).isEqualTo(Flt.FltEvent.Type.CREATE.toString());
        assertThat(fltEventJsonNode.has("flt")).isTrue();
        assertThat(fltEventJsonNode.get("flt").get("carrier").textValue()).isEqualTo(fltPostRequestPayload.get("carrier"));
        assertThat(fltEventJsonNode.get("flt").get("fltNum").textValue()).isEqualTo(fltPostRequestPayload.get("fltNum"));
        assertThat(fltEventJsonNode.get("flt").get("serviceType").textValue()).isEqualTo(fltPostRequestPayload.get("serviceType"));
        assertThat(fltEventJsonNode.get("flt").get("fltDate").textValue()).isEqualTo(fltPostRequestPayload.get("fltDate"));
        assertThat(fltEventJsonNode.get("flt").get("fltDow").numberValue()).isEqualTo(fltPostRequestPayload.get("fltDow"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").isArray()).isTrue();
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").size()).isEqualTo(1);
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depDate").textValue()).isEqualTo(fltLeg.get("depDate"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depDow").numberValue()).isEqualTo(fltLeg.get("depDow"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legDep").textValue()).isEqualTo(fltLeg.get("legDep"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legArr").textValue()).isEqualTo(fltLeg.get("legArr"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legSeqNum").numberValue()).isEqualTo(fltLeg.get("legSeqNum"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("schDepTime").textValue()).isEqualTo(fltLeg.get("schDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("schArrTime").textValue()).isEqualTo(fltLeg.get("schArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("estDepTime").textValue()).isEqualTo(fltLeg.get("estDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("estArrTime").textValue()).isEqualTo(fltLeg.get("estArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("actDepTime").textValue()).isEqualTo(fltLeg.get("actDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("actArrTime").textValue()).isEqualTo(fltLeg.get("actArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depTimeDiff").numberValue()).isEqualTo(fltLeg.get("depTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("arrTimeDiff").numberValue()).isEqualTo(fltLeg.get("arrTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("acReg").textValue()).isEqualTo(fltLeg.get("acReg"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("iataAcType").textValue()).isEqualTo(fltLeg.get("iataAcType"));

    }

    @Test
    @SneakyThrows
    void whenPatchUpdateFlt_thenUpdateTypeFltEventIsSentToKafka() {

        // create a flight, which will be updated via PATCH later
        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe));
        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("updateTestGroup", "true", this.embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "flt");

        // no message before PATCH
        ConsumerRecords<String, String> beforeUpdateRecords = KafkaTestUtils.getRecords(consumer, 100);
        assertThat(beforeUpdateRecords.count()).isZero();

        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>();
        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");
        fltPatchRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));
        // PATCH update fltLegs, add 2nd leg
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload)))
                .andExpect(status().is2xxSuccessful());

        // one FltEvent message is sent to Kafka
        ConsumerRecords<String, String> afterUpdateRecords = KafkaTestUtils.getRecords(consumer, 100);
        assertThat(afterUpdateRecords.count()).isOne();
        // verify the FltEvent message header and content
        ConsumerRecord<String, String> record = afterUpdateRecords.iterator().next();
        log.info(record.toString());
        assertThat(new String(record.headers().lastHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME).value(), StandardCharsets.UTF_8))
                .isEqualTo("fltEvent");
        JsonNode fltEventJsonNode = objectMapper.readTree(record.value());
        assertThat(fltEventJsonNode.has("id")).isTrue();
        assertThat(fltEventJsonNode.has("timestamp")).isTrue();
        assertThat(fltEventJsonNode.get("type").textValue()).isEqualTo(Flt.FltEvent.Type.UPDATE.toString());
        assertThat(fltEventJsonNode.has("flt")).isTrue();
        assertThat(fltEventJsonNode.get("flt").get("carrier").textValue()).isEqualTo(fltPostRequestPayload.get("carrier"));
        assertThat(fltEventJsonNode.get("flt").get("fltNum").textValue()).isEqualTo(fltPostRequestPayload.get("fltNum"));
        assertThat(fltEventJsonNode.get("flt").get("serviceType").textValue()).isEqualTo(fltPostRequestPayload.get("serviceType"));
        assertThat(fltEventJsonNode.get("flt").get("fltDate").textValue()).isEqualTo(fltPostRequestPayload.get("fltDate"));
        assertThat(fltEventJsonNode.get("flt").get("fltDow").numberValue()).isEqualTo(fltPostRequestPayload.get("fltDow"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").isArray()).isTrue();
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").size()).isEqualTo(2);

        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depDate").textValue()).isEqualTo(fltLegHkgTpe.get("depDate"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depDow").numberValue()).isEqualTo(fltLegHkgTpe.get("depDow"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legDep").textValue()).isEqualTo(fltLegHkgTpe.get("legDep"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legArr").textValue()).isEqualTo(fltLegHkgTpe.get("legArr"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("legSeqNum").numberValue()).isEqualTo(fltLegHkgTpe.get("legSeqNum"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("schDepTime").textValue()).isEqualTo(fltLegHkgTpe.get("schDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("schArrTime").textValue()).isEqualTo(fltLegHkgTpe.get("schArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("estDepTime").textValue()).isEqualTo(fltLegHkgTpe.get("estDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("estArrTime").textValue()).isEqualTo(fltLegHkgTpe.get("estArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("actDepTime").textValue()).isEqualTo(fltLegHkgTpe.get("actDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("actArrTime").textValue()).isEqualTo(fltLegHkgTpe.get("actArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("depTimeDiff").numberValue()).isEqualTo(fltLegHkgTpe.get("depTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("arrTimeDiff").numberValue()).isEqualTo(fltLegHkgTpe.get("arrTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("acReg").textValue()).isEqualTo(fltLegHkgTpe.get("acReg"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(0).get("iataAcType").textValue()).isEqualTo(fltLegHkgTpe.get("iataAcType"));

        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("depDate").textValue()).isEqualTo(fltLegTpeNrt.get("depDate"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("depDow").numberValue()).isEqualTo(fltLegTpeNrt.get("depDow"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("legDep").textValue()).isEqualTo(fltLegTpeNrt.get("legDep"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("legArr").textValue()).isEqualTo(fltLegTpeNrt.get("legArr"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("legSeqNum").numberValue()).isEqualTo(fltLegTpeNrt.get("legSeqNum"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("schDepTime").textValue()).isEqualTo(fltLegTpeNrt.get("schDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("schArrTime").textValue()).isEqualTo(fltLegTpeNrt.get("schArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("estDepTime").textValue()).isEqualTo(fltLegTpeNrt.get("estDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("estArrTime").textValue()).isEqualTo(fltLegTpeNrt.get("estArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("actDepTime").textValue()).isEqualTo(fltLegTpeNrt.get("actDepTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("actArrTime").textValue()).isEqualTo(fltLegTpeNrt.get("actArrTime"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("depTimeDiff").numberValue()).isEqualTo(fltLegTpeNrt.get("depTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("arrTimeDiff").numberValue()).isEqualTo(fltLegTpeNrt.get("arrTimeDiff"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("acReg").textValue()).isEqualTo(fltLegTpeNrt.get("acReg"));
        assertThat(fltEventJsonNode.get("flt").get("fltLegs").get(1).get("iataAcType").textValue()).isEqualTo(fltLegTpeNrt.get("iataAcType"));

    }

    @TestConfiguration
    static class KafkaConfig {

        /**
         * A {@link ProducerFactory} configured with the embedded Kafka, default key serializer,
         * and a value serializer {@link JsonSerializer} configured in the Spring context.
         *
         * @param jsonSerializer {@link com.yejianfengblue.sga.fltsch.config.KafkaConfig#jsonSerializer(ObjectMapper)}
         */
        @Primary
        @Bean
        public ProducerFactory<?, ?> embeddedKafkaProducerFactory(EmbeddedKafkaBroker embeddedKafka, JsonSerializer jsonSerializer) {

            Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
            producerProps.put(JsonSerializer.TYPE_MAPPINGS, "fltEvent:com.yejianfengblue.sga.fltsch.flt.Flt$FltEvent");
            DefaultKafkaProducerFactory<?, ?> producerFactory = new DefaultKafkaProducerFactory<>(
                    producerProps,
                    () -> null,
                    () -> jsonSerializer);
            return producerFactory;
        }
    }

}
