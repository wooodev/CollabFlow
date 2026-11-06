# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM gradle:8.8-jdk21-alpine AS builder
WORKDIR /app

# 의존성 레이어 캐시: build.gradle/settings.gradle 변경 시에만 재다운로드
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon --quiet

COPY src src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 비 root 유저로 실행 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
