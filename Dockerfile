# ---------- Стадия сборки ----------
FROM gradle:8.10-jdk17-alpine AS builder

WORKDIR /home/gradle/project

# Copy build files
COPY settings.gradle* build.gradle* gradle.properties* ./
COPY gradle ./gradle
COPY gradlew ./

RUN chmod +x gradlew

# Copy source code
COPY src ./src

# Build jar
RUN ./gradlew clean bootJar --no-daemon

# ---------- Стадия рантайма ----------
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy jar built by Gradle
COPY --from=builder /home/gradle/project/build/libs/*SNAPSHOT.jar app.jar

# Expose port
ENV PORT 8080

EXPOSE 8080

# Run the jar
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT} -jar /app/app.jar"]
