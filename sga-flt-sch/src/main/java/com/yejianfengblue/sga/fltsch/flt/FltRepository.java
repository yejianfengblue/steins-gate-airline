package com.yejianfengblue.sga.fltsch.flt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RepositoryRestResource(
        collectionResourceDescription = @Description("a collection of flight"),
        itemResourceDescription = @Description("flight"))
public interface FltRepository extends PagingAndSortingRepository<Flt, String> {

    @PreAuthorize("hasRole('flt-sch-user')")
    @Override
    Flt save(Flt flt);

    Page<Flt> findByFltDateBetween(LocalDate fltDateStart, LocalDate fltDateEnd,
                                   Pageable p);

    Page<Flt> findByCarrierAndFltNumAndFltDateBetween(String carrier, String fltNum,
                                                      @DateTimeFormat(iso = DATE) LocalDate fltDateStart,
                                                      @DateTimeFormat(iso = DATE) LocalDate fltDateEnd,
                                                      Pageable p);
}
