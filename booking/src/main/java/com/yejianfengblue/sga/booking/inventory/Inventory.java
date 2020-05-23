package com.yejianfengblue.sga.booking.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Document("inventories")
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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @EqualsAndHashCode.Include
    LocalDate fltDate;

    @NotNull
    @PositiveOrZero
    Integer available;

    @CreatedDate
    String createdDate;

    @LastModifiedDate
    String lastModifiedDate;

    public Inventory(String carrier,
                     String fltNum,
                     LocalDate fltDate,
                     Integer available) {

        this.carrier = carrier;
        this.fltNum = fltNum;
        this.fltDate = fltDate;
        this.available = available;

        registerEvent(InventoryEvent.of(this));
    }

    void setAvailable(Integer available) {

        this.available = available;

        registerEvent(InventoryEvent.of(this));
    }

    void add(Integer availableChange) {
        setAvailable(this.available + availableChange);
    }

    void subtract(Integer availableChange) {
        setAvailable(this.available - availableChange);
    }

}
