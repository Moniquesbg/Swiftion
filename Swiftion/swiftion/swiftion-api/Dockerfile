FROM maven:3.9.0-eclipse-temurin-19-focal

# Create a working directory
WORKDIR /app

# expose port
EXPOSE 8390

# set timezone
ENV TZ=Europe/Amsterdam
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Build the Maven project
ENTRYPOINT ["mvn", "spring-boot:run"]

