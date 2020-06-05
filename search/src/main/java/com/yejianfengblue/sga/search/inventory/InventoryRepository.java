package com.yejianfengblue.sga.search.inventory;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Optional;

interface InventoryRepository extends PagingAndSortingRepository<Inventory, String> {

    Optional<Inventory> findByCarrierAndFltNumAndFltDate(String carrier, String fltNum, LocalDate fltDate);
}
