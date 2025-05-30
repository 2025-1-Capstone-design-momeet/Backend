# =======================
# 1단계: 빌드 단계
# =======================
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# =======================
# 2단계: 실행 단계 (with FFmpeg)
# =======================
FROM eclipse-temurin:17-jdk

# ✅ FFmpeg 설치
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
