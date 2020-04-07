package com.yejianfengblue.sga.refdata.airport;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Document
public class Airport {

    @Id
    private String airportCode;

    private String name;

    private String municipality;

    private String countryCode;
}
