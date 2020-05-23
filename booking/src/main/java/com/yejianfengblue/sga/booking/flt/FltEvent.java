package com.yejianfengblue.sga.booking.flt;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
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
}