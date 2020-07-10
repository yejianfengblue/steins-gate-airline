package com.yejianfengblue.sga.search.flt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FltEventListener {

    public static final String TOPIC = "flt";

    private final FltEventHandler fltEventHandler;

    @KafkaListener(id = "search-group", topics = TOPIC)
    void listen(FltEvent fltEvent) {

        log.info("Receive from topic '{}' : {}", TOPIC, fltEvent);
        fltEventHandler.handle(fltEvent);
    }


}
