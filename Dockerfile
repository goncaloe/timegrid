FROM gradle:9.6.0-jdk21-alpine AS build
WORKDIR /app

# dependências primeiro, para aproveitar a cache enquanto só muda o código
COPY settings.gradle build.gradle ./
RUN gradle dependencies --no-daemon -q > /dev/null

COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S timegrid && adduser -S -G timegrid -H timegrid

COPY --from=build /app/build/libs/*.jar app.jar

USER timegrid
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
