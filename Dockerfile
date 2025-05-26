# 빌드 단계
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# 실행 단계
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
