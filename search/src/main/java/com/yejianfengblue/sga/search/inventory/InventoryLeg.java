package com.yejianfengblue.sga.search.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class InventoryLeg {

    @NotNull
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

    @NotNull
    @Min(-720) @Max(840)
    Integer depTimeDiff;

    @NotNull
    @Min(-720) @Max(840)
    Integer arrTimeDiff;

    @NotNull
    @PositiveOrZero
    Integer available;

    @JsonCreator
    public InventoryLeg(@JsonProperty("depDate") LocalDate depDate,
                        @JsonProperty("depDow") Integer depDow,
                        @JsonProperty("legDep") String legDep,
                        @JsonProperty("legArr") String legArr,
                        @JsonProperty("legSeqNum") Integer legSeqNum,
                        @JsonProperty("schDepTime") LocalDateTime schDepTime,
                        @JsonProperty("schArrTime") LocalDateTime schArrTime,
                        @JsonProperty("depTimeDiff") Integer depTimeDiff,
                        @JsonProperty("arrTimeDiff") Integer arrTimeDiff,
                        @JsonProperty("available") Integer available) {

        this.depDate = depDate;
        this.depDow = depDow;
        this.legDep = legDep;
        this.legArr = legArr;
        this.legSeqNum = legSeqNum;
        this.schDepTime = schDepTime;
        this.schArrTime = schArrTime;
        this.depTimeDiff = depTimeDiff;
        this.arrTimeDiff = arrTimeDiff;
        this.available = available;
    }
}