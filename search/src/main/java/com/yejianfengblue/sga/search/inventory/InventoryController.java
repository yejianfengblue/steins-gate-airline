package com.yejianfengblue.sga.search.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    private final PagedResourcesAssembler pagedResourcesAssembler;

    private final InventoryModelAssembler inventoryModelAssembler;

    @GetMapping("/{id}")
    public ResponseEntity getOne(@PathVariable("id") String id) {

        Optional<Inventory> inventory = inventoryRepository.findById(id);

        return inventory.isPresent() ?
                ResponseEntity.ok(inventory.get()) :
                ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity getAll(Pageable pageable) {

        Page<Inventory> inventories = inventoryRepository.findAll(pageable);

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(inventories, inventoryModelAssembler));
    }
}
