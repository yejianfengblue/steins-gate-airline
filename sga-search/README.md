# Search

This is a search application for flight inventory. To book a flight, go to application `booking`.

## RESTful API

The `index.html` in the current directory (search) provides more details about the RESTful APIs of this application. This HTML file is generated with Spring REST Docs, a test-driven approach which helps to guarantee the accuracy of API's documentation. In reality this HTML file should be published to web (either internal or external) rather than keep in Git. This HTML copy here is for demonstration as my applications are not running on a cloud server with a public IP address.

### Inventory

- List inventories
- Search inventories
- Get an inventory

## Adopted Framework and Library

- Spring Boot
- Spring Cloud Netflix
- Spring Cloud Stream (Kafka binder)
- Spring Data Mongo DB
- Spring Data REST
- Spring Kafka
- Spring REST Docs
- Spring Security 5
- [Lombok](https://projectlombok.org/)
- [Querydsl](http://querydsl.com/)

## Implementation Details

### Inventory Resource

Once an inventory event is received from Kafka, the inventory is stored in Mongo DB. 

Inventory is exposed as an read-only HTTP resource. Pagination, sorting and field filter are supported. Currently field filter only supports the "exactly equals" operator `=`. 

