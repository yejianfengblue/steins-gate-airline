package com.yejianfengblue.sga.fltsch.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.BooleanAssert;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class JsonSchemaUtil {

        /**
         * Assert the given field can be serialized
         *
         * @param beanPropertyDefinitions  can be fetched by
         * <pre>
         * {@code
         * objectMapper.getSerializationConfig()
         *             .introspect(objectMapper.constructType(YourClass.class))
         *             .findProperties();}
         * </pre>
         * @param propertyName  the JSON property name, in general the class field name
         */
        public static AbstractBooleanAssert<?> assertCouldSerialize(
                Collection<BeanPropertyDefinition> beanPropertyDefinitions,
                String propertyName) {

            Optional<BeanPropertyDefinition> propertyDefinition = beanPropertyDefinitions.stream()
                    .filter(serDefinition -> serDefinition.getName().equals(propertyName))
                    .findFirst();
            if (propertyDefinition.isPresent()) {
                return assertThat(propertyDefinition.get().couldSerialize());
            } else {
                log.debug("Property '{}' not found in the provided BeanPropertyDefinition collection", propertyName);
                return new BooleanAssert(false);
            }
        }

        /**
         * Assert the given field can be serialized
         *
         * @param objectMapper  the Jackson {@link ObjectMapper}
         */
        public static AbstractBooleanAssert<?> assertCouldSerialize(ObjectMapper objectMapper, Field field) {

            List<BeanPropertyDefinition> beanPropertyDefinitions = objectMapper.getSerializationConfig()
                    .introspect(objectMapper.constructType(field.getDeclaringClass()))
                    .findProperties();
            Optional<BeanPropertyDefinition> propertyDefinition = beanPropertyDefinitions.stream()
                    .filter(serDefinition -> serDefinition.getName().equals(field.getName()))
                    .findFirst();
            if (propertyDefinition.isPresent()) {
                return assertThat(propertyDefinition.get().couldSerialize());
            } else {
                log.debug("Can not get bean property definition from the provided ObjectMapper for field '{}' of class '{}'",
                        field.getName(), field.getDeclaringClass().getCanonicalName());
                return new BooleanAssert(false);
            }
        }

        /**
         * Assert the given field can be deserialized
         *
         * @param beanPropertyDefinitions  can be fetched by
         * <pre>
         * {@code
         * objectMapper.getDeserializationConfig()
         *             .introspect(objectMapper.constructType(YourClass.class))
         *             .findProperties();}
         * </pre>
         * @param propertyName  the JSON property name, in general the class field name
         */
        public static AbstractBooleanAssert<?> assertCouldDeserialize(
                Collection<BeanPropertyDefinition> beanPropertyDefinitions,
                String propertyName) {

            Optional<BeanPropertyDefinition> propertyDefinition = beanPropertyDefinitions.stream()
                    .filter(definition -> definition.getName().equals(propertyName))
                    .findFirst();

            if (propertyDefinition.isPresent()) {
                return assertThat(propertyDefinition.get().couldDeserialize());
            } else {
                log.debug("Property '{}' not found in the provided BeanPropertyDefinition collection", propertyName);
                return new BooleanAssert(false);
            }
        }

    /**
     * Assert the given field can be deserialized
     *
     * @param objectMapper  the Jackson {@link ObjectMapper}
     */
    public static AbstractBooleanAssert<?> assertCouldDeserialize(ObjectMapper objectMapper, Field field) {

        List<BeanPropertyDefinition> beanPropertyDefinitions = objectMapper.getDeserializationConfig()
                .introspect(objectMapper.constructType(field.getDeclaringClass()))
                .findProperties();
        Optional<BeanPropertyDefinition> propertyDefinition = beanPropertyDefinitions.stream()
                .filter(definition -> definition.getName().equals(field.getName()))
                .findFirst();
        if (propertyDefinition.isPresent()) {
            return assertThat(propertyDefinition.get().couldDeserialize());
        } else {
            log.debug("Can not get bean property definition from the provided ObjectMapper for field '{}' of class '{}'",
                    field.getName(), field.getDeclaringClass().getCanonicalName());
            return new BooleanAssert(false);
        }
    }
}
