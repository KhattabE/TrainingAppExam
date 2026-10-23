FROM eclipse-temurin:25-jre

# Installer curl
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Kopier den færdige JAR-fil
COPY target/app.jar /app.jar

# Applikationens port
EXPOSE 7070

# Start Java-applikationen
CMD ["java", "-jar", "/app.jar"]
