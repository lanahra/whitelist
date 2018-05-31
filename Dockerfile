FROM openjdk:8-jre-alpine

COPY target/whitelist-0.1.0.jar .

CMD ["java", "-jar", "./whitelist-0.1.0.jar"]
