package com.yejianfengblue.sga.search.flt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class FltLeg {

    @NotNull
    @JsonProperty
    LocalDate depDate;

    @NotNull
    @Min(1) @Max(7)
    Integer depDow;

    @NotNull
    @Size(min = 3, max = 3)
    String legDep;

    @NotNull
    @Size(min = 3, max = 3)
    String legArr;

    @NotNull
    @Min(1)
    Integer legSeqNum;

    @NotNull
    LocalDateTime schDepTime;

    @NotNull
    LocalDateTime schArrTime;

    LocalDateTime estDepTime;

    LocalDateTime estArrTime;

    LocalDateTime actDepTime;

    LocalDateTime actArrTime;

    @NotNull
    @Min(-720) @Max(840)
    Integer depTimeDiff;

    @NotNull
    @Min(-720) @Max(840)
    Integer arrTimeDiff;

    String acReg;

    @NotNull
    @Size(min = 3, max = 3)
    String iataAcType;

    public FltLeg(@JsonProperty("depDate") LocalDate depDate,
                  @JsonProperty("depDow") Integer depDow,
                  @JsonProperty("legDep") String legDep,
                  @JsonProperty("legArr") String legArr,
                  @JsonProperty("legSeqNum") Integer legSeqNum,
                  @JsonProperty("schDepTime") LocalDateTime schDepTime,
                  @JsonProperty("schArrTime") LocalDateTime schArrTime,
                  @JsonProperty("estDepTime") LocalDateTime estDepTime,
                  @JsonProperty("estArrTime") LocalDateTime estArrTime,
                  @JsonProperty("actDepTime") LocalDateTime actDepTime,
                  @JsonProperty("actArrTime") LocalDateTime actArrTime,
                  @JsonProperty("depTimeDiff") Integer depTimeDiff,
                  @JsonProperty("arrTimeDiff") Integer arrTimeDiff,
                  @JsonProperty("acReg") String acReg,
                  @JsonProperty("iataAcType") String iataAcType) {

        this.depDate = depDate;
        this.depDow = depDow;
        this.legDep = legDep;
        this.legArr = legArr;
        this.legSeqNum = legSeqNum;
        this.schDepTime = schDepTime;
        this.schArrTime = schArrTime;
        this.estDepTime = estDepTime;
        this.estArrTime = estArrTime;
        this.actDepTime = actDepTime;
        this.actArrTime = actArrTime;
        this.depTimeDiff = depTimeDiff;
        this.arrTimeDiff = arrTimeDiff;
        this.acReg = acReg;
        this.iataAcType = iataAcType;
    }
}
