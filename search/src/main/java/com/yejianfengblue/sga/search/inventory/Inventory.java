package com.yejianfengblue.sga.search.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yejianfengblue.sga.search.common.ServiceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Document("inventories")
@CompoundIndex(name = "fltKey", def = "{'carrier':1, 'fltNum':1, 'fltDate':1}", unique = true)
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})  // DB mapping uses the all args constructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Getter
@ToString
public class Inventory {

    @Id
    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
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

    Instant createdDate;

    Instant lastModifiedDate;

    @JsonCreator
    public Inventory(@JsonProperty("carrier") String carrier,
                     @JsonProperty("fltNum") String fltNum,
                     @JsonProperty("serviceType") ServiceType serviceType,
                     @JsonProperty("fltDate") LocalDate fltDate,
                     @JsonProperty("fltDow") Integer fltDow,
                     @JsonProperty("legs") List<InventoryLeg> legs,
                     @JsonProperty("createdDate") Instant createdDate,
                     @JsonProperty("lastModifiedDate") Instant lastModifiedDate) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.serviceType = serviceType;
        this.fltDate = fltDate;
        this.fltDow = fltDow;
        this.legs = legs;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }
}