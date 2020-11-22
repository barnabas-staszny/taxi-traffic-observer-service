# taxi-traffic-observer-service

## Requirements to try out the application:
 - installed docker

## Run and test the application:
##### 1. Start the application and database
Execute the following command in the root folder (the application docker image has been prebuilt and pushed to docker hub):
```sh
docker-compose up
```
##### 2. Quick check on the persisted data
Once the containers are up and running visit these 2 endpoints to list out data,
be aware that the statistics are generated and or updated once per minute so it needs time to create the first records.:
 - http://localhost:8080/taxirides
 - http://localhost:8080/statistics
##### 3. Connect to database for further checks
 - host: localhost
 - port: 5432
 - database: taxi-traffic
 - schema: taxi
 - user: taxi
 - password: taxi

## Tech stack of the application:
 - Java 8
 - Spring Boot
 - Spring Data (JPA used for simple DB operations, JDBC to execute complex queries.)
 - Google Cloud Pubsub
 - Flyway
 - Lombok
 - PostgreSql
 - Docker


## Business logic of the application:
There are 2 main running jobs implemeted.
##### Message receiver job:
Scheduled job which **runs on every node of a clustered microsevice subscribes** to the given Google Cloud Platform Pub/Sub topic with an Asynchronous Pull.
The job saves the messages into a taxi_ride table.
Deduplication solved by "upsert" behavior. The ```taxi_ride``` table has a composite primary key of ```(ride_id, ride_status)```. Whenever a message recieved multiple times the previous record is updated with the new values. This can also support maintenance scenarios where invalid values should be overriden.

##### Aggregator job:
Scheduled job which **runs on a single node of a clustered microsevice** starts the aggregation once per every minute.
The aggregator counts down the ```pickup```-s, ```dropoff```-s, ```pickupPassenger```-s and ```dropoffPassenger```-s and saves/updates those with a primary key of the reference of the date and hour (```hourOfAggregation```) of the aggregation data.
In the ```application.yml``` it can be configured that how many hours will be looked back for aggregation by this job. The property called ```taxi-traffic-observer.aggregator.backwardAggregationCycles```, by default it has been set to 50 hours. So the aggregator contionourly recalculates the last 50 hours of aggregations and upserts those into the ```aggregated_ride_stat``` table.
To keep the data allways consistent the **'Message receiver job' also initiates** asynchronously **an aggregation cycle** the the message_time is out of the configured scheduled aggregation cycles. For example in this case if there is a message which is 51 hours old then the aggregator job would skip to recalculate that hourly aggregation but the message receiver will initiate an aggregation for that as well.

## Credentials
In a real production scenario I would not push the sb-ems-dp-candidates-9cfc84b015ca.json to github and into the docker image but to make it easy to test the solution it is included now.

