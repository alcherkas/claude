# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- run stage ---
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/target/cart-service-*.jar app.jar
USER app
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
