package com.yejianfengblue.sga.booking.inventory;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Optional;

interface InventoryRepository extends PagingAndSortingRepository<Inventory, String> {

    Optional<Inventory> findByCarrierAndFltNumAndFltDate(String carrier, String fltNum, LocalDate fltDate);
}
