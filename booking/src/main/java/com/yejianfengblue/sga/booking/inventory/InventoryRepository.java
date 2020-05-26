package com.yejianfengblue.sga.booking.inventory;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface InventoryRepository extends PagingAndSortingRepository<Inventory, String> {

    Optional<Inventory> findByCarrierAndFltNumAndFltDate(String carrier, String fltNum, LocalDate fltDate);
}
