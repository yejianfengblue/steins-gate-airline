package com.yejianfengblue.sga.search.flt;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    public FltEvent(@JsonProperty("flt") Flt flt,
                    @JsonProperty("type") Type type,
                    @JsonProperty("id") UUID id,
                    @JsonProperty("timestamp") Instant timestamp) {

        this.flt = flt;
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
    }
}