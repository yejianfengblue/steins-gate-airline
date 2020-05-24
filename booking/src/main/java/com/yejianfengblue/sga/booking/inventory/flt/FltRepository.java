package com.yejianfengblue.sga.booking.inventory.flt;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

interface FltRepository extends MongoRepository<Flt, String> {

    Optional<Flt> findByCarrierAndFltNumAndFltDate(String carrier, String fltNum, LocalDate fltDate);
}
