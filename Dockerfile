# Use Maven base image with Java preinstalled
FROM maven:3.8.5-openjdk-17

# Set working directory
WORKDIR /app

# Copy all files to container
COPY . .

# Download dependencies
RUN mvn clean install -DskipTests

# Run the test suite
CMD ["mvn", "test", "-DsuiteXmlFile=testng.xml"]
