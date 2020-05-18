package com.yejianfengblue.sga.booking.booking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

@Document("bookings")
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@ToString
public class Booking {

    @Id
    @JsonIgnore
    String id;

    @NotNull
    @Size(min = 2, max = 2)
    String carrier;

    @NotNull
    @Size(min = 3, max = 5)
    String fltNum;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fltDate;

    @NotNull
    @Size(min = 3, max = 3)
    String segOrig;

    @NotNull
    @Size(min = 3, max = 3)
    String segDest;

    Money fare;

    Status status;

    @CreatedBy
    String createdBy;

    @CreatedDate
    Instant createdDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Instant lastModifiedDate;

    @JsonCreator
    public Booking(String carrier, String fltNum, LocalDate fltDate, String segOrig, String segDest) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.fltDate = fltDate;
        this.segOrig = segOrig;
        this.segDest = segDest;
        this.status = Status.DRAFT;
    }

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
    public LocalDate getFltDate() {
        return fltDate;
    }

    @JsonGetter
    public String getSegOrig() {
        return segOrig;
    }

    @JsonGetter
    public String getSegDest() {
        return segDest;
    }

    @JsonGetter
    public Money getFare() {
        return fare;
    }

    @JsonGetter
    public Status getStatus() {
        return status;
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

    @JsonSetter
    void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    @JsonSetter
    void setFltNum(String fltNum) {
        this.fltNum = fltNum;
    }

    @JsonSetter
    void setFltDate(LocalDate fltDate) {
        this.fltDate = fltDate;
    }

    @JsonSetter
    void setSegOrig(String segOrig) {
        this.segOrig = segOrig;
    }

    @JsonSetter
    void setSegDest(String segDest) {
        this.segDest = segDest;
    }

    @JsonIgnore
    void setFare(Money fare) {
        this.fare = fare;
    }

    /**
     * Booking status indicator
     */
    enum Status {

        /**
         * The booking is drafted but not confirmed yet.
         * Next status may be CONFIRMED or CANCELLED.
         */
        DRAFT,

        /**
         * The booking is confirmed. No changes allowed to it anymore.
         * Next status may be CHECKED_IN or CANCELLED.
         */
        CONFIRMED,

        /**
         * The booking is cancelled.
         * Status change is not allowed anymore.
         */
        CANCELLED,

        /**
         * The booking is checked in.
         * Status change is not allowed anymore.
         */
        CHECKED_IN,
    }
}
