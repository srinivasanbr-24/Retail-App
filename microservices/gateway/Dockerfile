FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Ensure you are copying ALL modules (inventory, payment, gateway) and the root pom.xml
COPY pom.xml ./
COPY inventory/ ./inventory/
COPY payment/ ./payment/
COPY gateway/ ./gateway/

# The RUN command is correct for the gateway service
RUN mvn -f pom.xml clean package -pl gateway -am -DskipTests


FROM eclipse-temurin:17-jre AS final

ARG SERVICE_PORT=8082

WORKDIR /app

# The COPY command is correct as long as the ARG JAR_FILE is correct
COPY --from=build /app/gateway/target/gateway.jar /app/gateway.jar

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["java", "-jar", "gateway.jar"]