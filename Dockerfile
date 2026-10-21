
# Java 17
FROM eclipse-temurin:25-jre

# Installer curl
RUN apk update && apk add --no-cache curl

# Kopier den færdige JAR-fil
COPY target/app.jar /app.jar

# Applikationens port
EXPOSE 7070

# Start Java-applikationen
CMD ["java", "-jar", "/app.jar"]
