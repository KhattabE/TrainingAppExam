
# Java 17
FROM amazoncorretto:17-alpine

# Installer curl
RUN apk update && apk add --no-cache curl

# Kopier den færdige JAR-fil
COPY target/app.jar /app.jar

# Applikationens port
EXPOSE 7070

# Start Java-applikationen
CMD ["java", "-jar", "/app.jar"]
