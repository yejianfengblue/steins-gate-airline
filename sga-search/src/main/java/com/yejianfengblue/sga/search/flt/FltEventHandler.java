package com.yejianfengblue.sga.search.flt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FltEventHandler {

    private final FltRepository fltRepository;

    void handle(FltEvent fltEvent) {

        Flt flt = fltEvent.getFlt();
        Optional<Flt> foundFlt = fltRepository.findByCarrierAndFltNumAndFltDate(flt.getCarrier(), flt.getFltNum(), flt.getFltDate());

        foundFlt.ifPresent(f -> flt.setId(f.getId()));

        fltRepository.save(flt);
    }
}
