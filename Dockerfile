FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 featherlog
COPY --from=build /app/target/featherlog-*.jar app.jar
USER featherlog
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
