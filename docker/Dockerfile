# Build stage
FROM maven:3.6.0-openjdk-17-alpine AS builder
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Final stage: Add Certbot and Nginx for SSL handling
FROM openjdk:8-jdk-alpine

# Install Nginx and Certbot
RUN apk add --no-cache nginx certbot python3 py3-pip && \
    pip3 install --upgrade certbot && \
    apk add --no-cache --virtual .build-deps gcc libffi-dev musl-dev && \
    pip3 install cryptography && \
    apk del .build-deps

# Expose ports
EXPOSE 80 443

# Copy application JAR
ENV APP_HOME /usr/src/app
COPY --from=builder /usr/src/app/target/shopping-cart-0.0.1-SNAPSHOT.jar $APP_HOME/app.jar

# Nginx configuration file
COPY nginx.conf /etc/nginx/nginx.conf

# Start Nginx and the application
CMD ["sh", "-c", "nginx && java -jar $APP_HOME/app.jar"]
