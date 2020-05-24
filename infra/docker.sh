docker-compose -f docker-compose-kafka.yml -f docker-compose-mongodb.yml up

docker exec -it mongo mongo

rs.initiate()

exit