# we will use openjdk 8 
FROM openjdk:8

EXPOSE 8080

# copy the packaged jar file into our docker image
COPY target/traffic-observer-0.0.1-SNAPSHOT.jar /taxi-traffic-observer-service.jar
ADD src/main/resources/sb-ems-dp-candidates-9cfc84b015ca.json src/main/resources/sb-ems-dp-candidates-9cfc84b015ca.json

# set the startup command to execute the jar
CMD ["java", "-jar", "/taxi-traffic-observer-service.jar"]