# ---- stage 1: build the React app ----
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY super-market-inv-backend/package*.json ./
RUN npm ci
COPY super-market-inv-backend/ ./
RUN npm run build

# ---- stage 2: build the Spring Boot jar, with dist/ baked in as static content ----
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY super-market-inv-frontend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY super-market-inv-frontend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn -B clean package -DskipTests

# ---- stage 3: run only the jar, nothing else ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
