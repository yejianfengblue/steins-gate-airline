package com.yejianfengblue.sga.booking.inventory.flt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FltLeg {

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

}
