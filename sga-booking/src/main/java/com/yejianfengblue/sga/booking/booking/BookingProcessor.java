package com.yejianfengblue.sga.booking.booking;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static com.yejianfengblue.sga.booking.booking.Booking.Status.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A {@link RepresentationModelProcessor} that takes an {@link Booking} that has been wrapped by Spring Data REST into
 * an {@link EntityModel} and applies custom Spring HATEOAS-based {@link Link}s based on the state.
 */
@Component
public class BookingProcessor implements RepresentationModelProcessor<EntityModel<Booking>> {

    @Override
    public EntityModel<Booking> process(EntityModel<Booking> model) {

        BookingController bookingController = methodOn(BookingController.class);


        if (valid(model.getContent().getStatus(), CONFIRMED)) {

            model.add(
                    linkTo(bookingController.confirm(model.getContent().getId()))
                            .withRel("confirm"));
        }

        if (valid(model.getContent().getStatus(), CHECKED_IN)) {

            model.add(
                    linkTo(bookingController.checkIn(model.getContent().getId()))
                            .withRel("check-in"));
        }

        if (valid(model.getContent().getStatus(), CANCELLED)) {

            model.add(
                    linkTo(bookingController.cancel(model.getContent().getId()))
                            .withRel("cancel"));
        }

        return model;
    }

}
