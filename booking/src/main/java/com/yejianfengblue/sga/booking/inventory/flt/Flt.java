package com.yejianfengblue.sga.booking.inventory.flt;

import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Value
public class Flt {

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
    List<FltLeg> fltLegs = new ArrayList<>();

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
