package com.yejianfengblue.sga.booking.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
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

    InventoryLeg(FltEvent.FltLeg fltLeg, Integer available) {

        this.depDate = fltLeg.getDepDate();
        this.depDow = fltLeg.getDepDow();
        this.legDep = fltLeg.getLegDep();
        this.legArr = fltLeg.getLegArr();
        this.legSeqNum = fltLeg.getLegSeqNum();
        this.schDepTime = fltLeg.getSchDepTime();
        this.schArrTime = fltLeg.getSchArrTime();
        this.depTimeDiff = fltLeg.getDepTimeDiff();
        this.arrTimeDiff = fltLeg.getArrTimeDiff();
        this.available = available;
    }

    void addAvailable(@NotNull @Positive Integer change) {
        this.available += change;
    }

    void subtractAvailable(@NotNull @Positive Integer change) {
        this.available -= change;
    }

}
