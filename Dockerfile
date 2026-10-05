# ---------- Frontend ----------
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build


# ---------- Backend ----------
FROM maven:3.9.11-eclipse-temurin-22 AS backend-build
WORKDIR /app

COPY backend/pom.xml ./backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline -B

COPY backend/src ./backend/src
COPY --from=frontend-build /app/frontend/dist ./backend/src/main/resources/static

RUN mvn -f backend/pom.xml clean package -DskipTests -B


# ---------- Runtime ----------
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/backend/target/study-buddy-1.0.0.jar app.jar

ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-10000}"]
