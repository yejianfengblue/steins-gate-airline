package com.yejianfengblue.sga.booking.config;

import com.yejianfengblue.sga.booking.common.MoneyReadConverter;
import com.yejianfengblue.sga.booking.common.MoneyWriteConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

    // reuse spring boot properties spring.data.mongodb.*
    @NonNull
    private final MongoProperties properties;

    @Override
    protected String getDatabaseName() {
        return properties.getDatabase();
    }

    @Override
    protected void configureConverters(MongoCustomConversions.MongoConverterConfigurationAdapter adapter) {

        adapter.useNativeDriverJavaTimeCodecs();
        adapter.registerConverter(new MoneyReadConverter());
        adapter.registerConverter(new MoneyWriteConverter());
    }

}