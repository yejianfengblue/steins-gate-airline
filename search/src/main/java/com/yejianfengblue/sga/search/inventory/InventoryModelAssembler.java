package com.yejianfengblue.sga.search.inventory;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryModelAssembler implements RepresentationModelAssembler<Inventory, EntityModel<Inventory>> {

    @Override
    public EntityModel<Inventory> toModel(Inventory inventory) {

        return EntityModel.of(inventory,
                linkTo(methodOn(InventoryController.class).getOne(inventory.getId()))
                        .withSelfRel());
    }
}
