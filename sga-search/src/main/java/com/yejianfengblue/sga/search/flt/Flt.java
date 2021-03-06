package com.yejianfengblue.sga.search.flt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yejianfengblue.sga.search.common.ServiceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document("flts")
@CompoundIndex(name = "fltKey", def = "{'carrier':1, 'fltNum':1, 'fltDate':1}", unique = true)
@Data
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Flt {

    @Id
    String id;

    @NotNull
    @Size(min = 2, max = 2)
    @EqualsAndHashCode.Include
    String carrier;

    @NotNull
    @Size(min = 3, max = 5)
    @EqualsAndHashCode.Include
    String fltNum;

    @NotNull
    ServiceType serviceType;

    @NotNull
    @EqualsAndHashCode.Include
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fltDate;

    @NotNull
    @Min(1) @Max(7)
    Integer fltDow;

    @NotEmpty
    @Valid
    List<FltLeg> fltLegs;

    String createdBy;

    Instant createdDate;

    String lastModifiedBy;

    Instant lastModifiedDate;

    @JsonCreator
    public Flt(@JsonProperty("carrier") String carrier,
               @JsonProperty("fltNum") String fltNum,
               @JsonProperty("serviceType") ServiceType serviceType,
               @JsonProperty("fltDate") LocalDate fltDate,
               @JsonProperty("fltDow") Integer fltDow,
               @JsonProperty("fltLegs") List<FltLeg> fltLegs,
               @JsonProperty("createdBy") String createdBy,
               @JsonProperty("createdDate") Instant createdDate,
               @JsonProperty("lastModifiedBy") String lastModifiedBy,
               @JsonProperty("lastModifiedDate") Instant lastModifiedDate) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.serviceType = serviceType;
        this.fltDate = fltDate;
        this.fltDow = fltDow;
        this.fltLegs = null != fltLegs ? fltLegs : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
    }
}
