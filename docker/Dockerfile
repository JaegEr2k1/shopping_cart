# Use a Maven image to build the application
FROM maven:3.8.4-openjdk-8 as builder

# Set the working directory
WORKDIR /usr/src/app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use a smaller base image for the final image
FROM openjdk:8u151-jdk-alpine3.7

# Set the application home
ENV APP_HOME /usr/src/app

# Copy the jar file from the builder stage
COPY --from=builder /usr/src/app/target/shopping-cart-0.0.1-SNAPSHOT.jar $APP_HOME/app.jar

# Expose the application port
EXPOSE 8070

# Set the working directory
WORKDIR $APP_HOME

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
