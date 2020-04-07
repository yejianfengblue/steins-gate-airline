package com.yejianfengblue.sga.refdata.airport;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource
public interface AirportRepository extends PagingAndSortingRepository<Airport, String>,
        QuerydslPredicateExecutor<Airport>  {

    @Override
    @RestResource(exported = false)  // hide HTTP method DELETE
    void deleteById(String id);

    @Override
    @RestResource(exported = false)  // hide HTTP method DELETE
    void delete(Airport airport);

    @Override
    @RestResource(exported = false)  // hide HTTP method POST, PUT, PATCH
    Airport save(Airport airport);
}
