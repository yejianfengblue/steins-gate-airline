package com.yejianfengblue.sga.booking.config;

import com.yejianfengblue.sga.booking.common.MoneyReadConverter;
import com.yejianfengblue.sga.booking.common.MoneyWriteConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;

import javax.validation.Validator;

@Configuration
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

    // reuse spring boot properties spring.data.mongodb.*
    private final MongoProperties properties;

    @Override
    protected String getDatabaseName() {
        return properties.getDatabase();
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }

    @Override
    protected void configureConverters(MongoCustomConversions.MongoConverterConfigurationAdapter adapter) {

        adapter.useNativeDriverJavaTimeCodecs();
        adapter.registerConverter(new MoneyReadConverter());
        adapter.registerConverter(new MoneyWriteConverter());
    }

    // enable JSR-303 validation before save to Mongo DB
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(Validator validator) {
        return new ValidatingMongoEventListener(validator);
    }

}