package com.yejianfengblue.sga.booking.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yejianfengblue.sga.booking.common.ServiceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Document("inventories")
@CompoundIndex(name = "fltKey", def = "{'carrier':1, 'fltNum':1, 'fltDate':1}", unique = true)
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Getter
@ToString
public class Inventory extends AbstractAggregateRoot<Inventory> {

    @Id
    @JsonIgnore
    String id;

    @NotNull
    @Size(min = 2, max = 2)
    @EqualsAndHashCode.Include
    String carrier;

    @NotNull
    @Size(min = 3, max = 5)
    @EqualsAndHashCode.Include
    String fltNum;

    @NotNull
    ServiceType serviceType;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @EqualsAndHashCode.Include
    LocalDate fltDate;

    @NotNull
    @Min(1) @Max(7)
    Integer fltDow;

    @NotEmpty
    @Valid
    List<InventoryLeg> legs;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    public Inventory(String carrier,
                     String fltNum,
                     ServiceType serviceType,
                     LocalDate fltDate,
                     Integer fltDow,
                     List<InventoryLeg> inventoryLegs) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.serviceType = serviceType;
        this.fltDate = fltDate;
        this.fltDow = fltDow;
        this.legs = inventoryLegs;

        registerEvent(InventoryEvent.of(this));
    }

    public Inventory(FltEvent.Flt flt, Integer available) {

        this(flt.getCarrier(), flt.getFltNum(), flt.getServiceType(),
                flt.getFltDate(), flt.getFltDow(),
                flt.getFltLegs().stream()
                        .map(leg -> new InventoryLeg(leg, available))
                        .collect(Collectors.toList()));
    }

    /**
     * Validate whether the given segment is a valid combination of the legs
     *
     * @see #findLegs(String, String)
     */
    public boolean isValidSegment(@NotNull String segOrig, @NotNull String segDest) {

        return !findLegs(segOrig, segDest).isEmpty();
    }

    /**
     * Returns an unmodifiable List containing the legs of the given segment.
     *
     * @return a non-empty list if a combination of legs matches the given segment,
     * otherwise an empty list.
     * @see #isValidSegment(String, String)
     */
    List<InventoryLeg> findLegs(@NotNull String segOrig, @NotNull String segDest) {

        Optional<InventoryLeg> depLeg = legs.stream().filter(l -> segOrig.equals(l.getLegDep())).findFirst();
        Optional<InventoryLeg> arrLeg = legs.stream().filter(l -> segDest.equals(l.getLegArr())).findFirst();

        if (depLeg.isPresent() && arrLeg.isPresent() &&
                depLeg.get().getLegSeqNum() <= arrLeg.get().getLegSeqNum()) {

            return List.copyOf(
                    legs.subList(legs.indexOf(depLeg.get()), legs.indexOf(arrLeg.get()) + 1));
        } else {
            return List.of();
        }
    }

    /**
     * Get the inventory available of the given segment, which is the min of inventory available of the segment legs.
     *
     * @return the inventory available or 0 if the segment doesn't exist.
     */
    public Integer getAvailable(@NotNull String segOrig,
                                @NotNull String segDest) {

        List<InventoryLeg> legs = findLegs(segOrig, segDest);
        return legs.stream()
                .mapToInt(InventoryLeg::getAvailable)
                .min()
                .orElse(0);
    }

    /**
     * Add available to the legs which belong to the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link #isValidSegment(String, String)}.
     */
    void addAvailable(@NotNull String segOrig,
                      @NotNull String segDest,
                      @NotNull @Positive Integer availableChange) {

        List<InventoryLeg> legs = findLegs(segOrig, segDest);
        legs.forEach(leg -> leg.addAvailable(availableChange));

        registerEvent(InventoryEvent.of(this));
    }

    /**
     * Subtract available from the legs which belong to the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link #isValidSegment(String, String)}.
     */
    void subtractAvailable(@NotNull String segOrig,
                           @NotNull String segDest,
                           @NotNull @Positive Integer availableChange) {

        List<InventoryLeg> legs = findLegs(segOrig, segDest);
        legs.forEach(leg -> leg.subtractAvailable(availableChange));

        registerEvent(InventoryEvent.of(this));
    }

    /**
     * Set available to the legs which belong to the given segment.
     * If the segment doesn't exist, no change is made.
     * Segment existence can be checked by {@link #isValidSegment(String, String)}.
     */
    void setAvailable(@NotNull String segOrig,
                      @NotNull String segDest,
                      @NotNull @Positive Integer available) {

        List<InventoryLeg> legs = findLegs(segOrig, segDest);
        legs.forEach(leg -> leg.setAvailable(available));

        registerEvent(InventoryEvent.of(this));
    }

}
