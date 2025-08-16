FROM openjdk:21-jdk-slim

WORKDIR /app
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

# 비루트 사용자
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]