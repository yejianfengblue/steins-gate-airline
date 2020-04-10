package com.yejianfengblue.sga.fltsch.flt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.rest.core.annotation.Description;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FltLeg {

    @Description("departure date of departure airport, local timezone")
    LocalDate depDate;

    @Description("day of week of departure date, from 1 (Monday) to 7 (Sunday)")
    Integer depDow;

    @Description("flight leg departure airport")
    String legDep;

    @Description("flight leg arrival airport")
    String legArr;

    @Description("flight leg sequence number of flight routing")
    Integer legSeqNum;

    @Description("scheduled departure time, in local timezone of departure airport")
    LocalDateTime schDepTime;

    @Description("scheduled arrival time, in local timezone of arrival airport")
    LocalDateTime schArrTime;

    @Description("estimated departure time, in local timezone of departure airport")
    LocalDateTime estDepTime;

    @Description("estimated arrival time, in local timezone of arrival airport")
    LocalDateTime estArrTime;

    @Description("actual departure time, in local timezone of departure airport")
    LocalDateTime actDepTime;

    @Description("actual arrival time, in local timezone of arrival airport")
    LocalDateTime actArrTime;

    @Description("time difference of the timezone of departure airport")
    Integer depTimeDiff;

    @Description("time difference of the timezone of arrival airport")
    Integer arrTimeDiff;

    @Description("aircraft registration, alternatively called tail number")
    String acReg;

    @Description("IATA aircraft type code")
    String iataAcType;

}
