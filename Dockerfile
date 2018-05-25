FROM openjdk:8-jre-alpine

CMD ["java", "-jar", "/root/.m2/repository/com/lanahra/whitelist/whitelist/0.1.0/whitelist-0.1.0.jar"]
