package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.annotation.*;
import com.yejianfengblue.sga.fltsch.constant.ServiceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document("flt")
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class Flt {

    @Id
    @JsonIgnore
    String id;

    @NotNull
    @Size(min = 2, max = 2)
    @Description("airline carrier code")
    String carrier;

    @NotNull
    @Size(min = 3, max = 5)
    @Description("flight number")
    String fltNum;

    @NotNull
    // comment out serviceType description, as Spring Data Rest has NPE BUG when generate enum description
//    @Description("PAX for passenger, FRTR for freighter")
    ServiceType serviceType;

    @NotNull
    @Description("flight date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fltDate;

    @NotNull
    @Min(1) @Max(7)
    @Description("day of week of flight date, from 1 (Monday) to 7 (Sunday)")
    Integer fltDow;

    @NotEmpty
    @Valid
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
        this.fltLegs = null != fltLegs ? fltLegs : new ArrayList<>();
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonGetter
    public String getCarrier() {
        return carrier;
    }

    @JsonGetter
    public String getFltNum() {
        return fltNum;
    }

    @JsonGetter
    public ServiceType getServiceType() {
        return serviceType;
    }

    @JsonGetter
    public LocalDate getFltDate() {
        return fltDate;
    }

    @JsonGetter
    public Integer getFltDow() {
        return fltDow;
    }

    @JsonGetter
    public List<FltLeg> getFltLegs() {
        return fltLegs;
    }

    @JsonGetter
    public String getCreatedBy() {
        return createdBy;
    }

    @JsonGetter
    public Instant getCreatedDate() {
        return createdDate;
    }

    @JsonGetter
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @JsonGetter
    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setCarrier(String carrier) {
        if (null == this.carrier)
            this.carrier = carrier;
    }

    public void setFltNum(String fltNum) {
        if (null == this.fltNum)
            this.fltNum = fltNum;
    }

    public void setServiceType(ServiceType serviceType) {
        if (null == this.serviceType)
            this.serviceType = serviceType;
    }

    public void setFltDate(LocalDate fltDate) {
        if (null == this.fltDate)
            this.fltDate = fltDate;
    }

    public void setFltDow(Integer fltDow) {
        if (null == this.fltDow)
            this.fltDow = fltDow;
    }

    @JsonSetter
    public void setFltLegs(List<FltLeg> fltLegs) {
        this.fltLegs = null != fltLegs ? fltLegs : new ArrayList<>();
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

    @Value
    static class FltEvent {

        Flt flt;

        Type type;

        UUID id;

        Instant timestamp;

        static FltEvent of(Flt flt, Type type) {
            return new FltEvent(flt, type, UUID.randomUUID(), Instant.now());
        }

        private FltEvent(Flt flt, Type type, UUID id, Instant timestamp) {

            this.flt = flt;
            this.type = type;
            this.id = id;
            this.timestamp = timestamp;
        }

        enum Type {
            CREATE, UPDATE
        }
    }
}
