FROM eclipse-temurin:21-jre-jammy

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
