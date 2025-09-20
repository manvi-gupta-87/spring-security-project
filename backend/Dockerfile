# Multi-stage build for smaller image size
 FROM maven:3.9.4-eclipse-temurin-21 AS builder

  #What this does:
   #- FROM: Starting point - use an existing image that has Maven and Java 21
   #- AS builder: Name this stage "builder" (we'll reference it later)
   #- Why multi-stage? The Maven image is 800MB+, but we only need it to build, not run

 WORKDIR /app
    #- WORKDIR: Like cd /app - sets working directory inside container
    #- Creates /app if it doesn't exist

 COPY pom.xml .
 RUN mvn dependency:go-offline
    #- COPY pom.xml .: Copy pom.xml from your machine to container
    #- RUN mvn dependency:go-offline: Download all dependencies
    #- Why separate? Docker caches each step. If code changes but pom.xml doesn't, it uses cached dependencies (faster builds!)

 # Copy source code
   COPY src ./src
   # Build the application
   RUN mvn clean package -DskipTests
   #- COPY src ./src: Copy your source code
   #- RUN mvn clean package -DskipTests: Build the JAR file
   #- -DskipTests: Skip tests during Docker build (faster)

 #Part 2: Runtime Stage
FROM eclipse-temurin:21-jre-alpine
  #- Start fresh with a smaller image (JRE only, not JDK)
  #- alpine: Minimal Linux (5MB vs 100MB+)
  #- This image is ~180MB vs 800MB for full Maven image!

 WORKDIR /app
   # Copy the built JAR from builder stage
 COPY --from=builder /app/target/*.jar app.jar
   #- COPY --from=builder: Copy from the first stage
   #- Takes the JAR we built and puts it in this smaller image
   #- Magic: We built with 800MB image but run with 180MB image!

 # Create non-root user for security
 RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
 USER appuser
   #- Security best practice: Don't run as root user
   #- Creates a user called "appuser" and switches to it
   #- If container is compromised, attacker doesn't have root access

# Expose port
  EXPOSE 8080
  #- Documents that this container listens on port 8080
  #- Doesn't actually open the port (that's done with docker run -p)

 # Health check
   HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
         CMD wget --no-verbose --tries=1 --spider http://localhost:8080/hello || exit 1
   #- Docker checks if your app is healthy
   #- Every 30s, calls /health endpoint
   #- If fails 3 times, marks container as unhealthy

   # Run the application
   ENTRYPOINT ["java", "-jar", "/app/app.jar"]
   #- The command that runs when container starts
   #- Like the main method for your container