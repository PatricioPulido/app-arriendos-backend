# syntax=docker/dockerfile:1
# Imagen de produccion de la API. Sirve para Render, Koyeb, Railway, Fly u OCI.
#   docker build -t arriendos-api .
#   docker run --env-file .env -p 8080:8080 arriendos-api

# ---------- 1) Compilar ----------
# Imagen con Maven incluido: evita depender de mvnw (que se rompe si git lo
# guarda con saltos de linea de Windows)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero solo el pom para bajar dependencias (se cachea entre builds)
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests && cp target/*.jar app.jar

# ---------- 2) Ejecutar ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Usuario sin privilegios
RUN useradd --system --uid 1001 spring
COPY --from=build /app/app.jar app.jar
USER spring

# Pensado para planes chicos (512 MB): el heap usa como maximo el 70% de la RAM
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
# Las plataformas inyectan PORT; si no, 8080
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
