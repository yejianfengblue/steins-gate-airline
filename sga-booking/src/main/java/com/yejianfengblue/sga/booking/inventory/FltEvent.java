package com.yejianfengblue.sga.booking.inventory;

import com.yejianfengblue.sga.booking.common.ServiceType;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
class FltEvent {

    @Valid
    Flt flt;

    @NotNull
    Type type;

    @NotNull
    UUID id;

    @NotNull
    Instant timestamp;

    enum Type {
        CREATE, UPDATE
    }

    @Value
    static class Flt {

        @NotNull
        @Size(min = 2, max = 2)
        String carrier;

        @NotNull
        @Size(min = 3, max = 5)
        String fltNum;

        @NotNull
        ServiceType serviceType;

        @NotNull
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

        public boolean isCompleteRouting() {

            // first leg sequence number must be 1
            if (getFltLegs().get(0).getLegSeqNum() != 1) {
                return false;
            }

            Integer lastLegSeqNum = getFltLegs().get(0).getLegSeqNum();
            String lastLegArr = getFltLegs().get(0).getLegArr();
            for (int i = 1; i < getFltLegs().size(); i++) {

                FltLeg currentLeg = getFltLegs().get(i);

                // leg sequence number must be sequential
                if (currentLeg.getLegSeqNum() != lastLegSeqNum+1) {
                    return false;
                }

                // the dep and arr of legs must be linked end to end
                if (!currentLeg.getLegDep().equals(lastLegArr)) {
                    return false;
                }

                lastLegSeqNum = currentLeg.getLegSeqNum();
                lastLegArr = currentLeg.getLegArr();
            }

            return true;
        }

    }

    @Value
    static class FltLeg {

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
}