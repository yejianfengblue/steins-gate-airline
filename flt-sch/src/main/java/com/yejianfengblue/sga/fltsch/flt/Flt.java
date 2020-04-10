package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yejianfengblue.sga.fltsch.constant.ServiceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Document("flt")
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class Flt {

    @Id
    String id;

    @Description("airline carrier code")
    String carrier;

    @Description("flight number")
    String fltNum;

    // comment out serviceType description, as Spring Data Rest has NPE BUG when generate enum description
//    @Description("PAX for passenger, FRTR for freighter")
    ServiceType serviceType;

    @Description("flight date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fltDate;

    @Description("day of week of flight date, from 1 (Monday) to 7 (Sunday)")
    Integer fltDow;

    @Description("flight leg collection")
    List<FltLeg> fltLegs = new ArrayList<>();

    @CreatedBy
    String createdBy;

    @CreatedDate
    Instant createdDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Instant lastModifiedDate;

    @JsonCreator
    public Flt(String carrier, String fltNum, ServiceType serviceType, LocalDate fltDate, Integer fltDow, List<FltLeg> fltLegs) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.serviceType = serviceType;
        this.fltDate = fltDate;
        this.fltDow = fltDow;
        this.fltLegs = fltLegs;
    }

    /**
     *
     * @return the concatenation of carrier and fltNum, for example "SG001"
     */
    @Transient
    @JsonIgnore
    public String getFltDesignator() {
        return carrier + fltNum;
    }

    /**
     *
     * @return the concatenation of carrier, fltNum and fltDate,
     * for example "SG001/01JAN20" for flight SG001 on 01 January 2020
     */
    @Transient
    @JsonIgnore
    public String getFltDesignatorAndFltDate() {
        return getFltDesignator() + "/" + fltDate.format(DateTimeFormatter.ofPattern("ddMMMyy")).toUpperCase();
    }

    void addFltLeg(FltLeg fltLeg) {
        this.fltLegs.add(fltLeg);
    }

    void removeFltLeg(FltLeg fltLeg) {
        this.fltLegs.remove(fltLeg);
    }

}
