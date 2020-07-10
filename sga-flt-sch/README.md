# Flight Schedule

This is a flight schedule management application. 

## RESTful API

The `index.html` in the current directory (flt-sch) provides more details about the RESTful APIs of this application. This HTML file is generated with Spring REST Docs, a test-driven approach which helps to guarantee the accuracy of API's documentation. In reality this HTML file should be published to web (either internal or external) rather than keep in Git. This HTML copy here is for demonstration as my applications are not running on a cloud server with a public IP address.

### Flight

- List flights
- Create a flight: only allowed for user with role `flt-sch-user`
- Get a flight
- Update a flight: only allowed for user with role `flt-sch-user`

Currently, deletion of a flight is not allowed. In fact deletion should be handled by a flight status. The status is not implemented for simplicity. If you're interested in status handling, see the application `Booking` which demonstrates status transition.

## Adopted Framework and Library

- Spring Boot
- Spring Cloud Netflix
- Spring Data Mongo DB
- Spring Data REST
- Spring Kafka
- Spring REST Docs
- Spring Security 5
- [Lombok](https://projectlombok.org/)

## Implementation Details

### Flight Resource

Flight is persisted to Mongo DB. A unique index guarantees the uniqueness of a flight. 

Spring Data REST exposes the flight domain as HTTP resources. Java and Jackson getters and setters guarantees what properties can be serialized to JSON and seen by clients, and what properties can be modified by clients. JSR-303 and JSR-380 bean validation are used for flight input data validation.

Auditing are enabled to record created user and time, and last modified user and time. The user info is retrieved from the OAuth 2.0 JWT.

When a flight is created or updated, a flight event is sent to Kafka.

