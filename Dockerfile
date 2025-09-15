FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_FILE=target/bank-monolith-*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/keys/jwt-public.pem /app/keys/jwt-public.pem
COPY src/main/resources/keys/jwt-private.pem /app/keys/jwt-private.pem
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]