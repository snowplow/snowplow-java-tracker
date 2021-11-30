FROM openjdk:11
COPY . /java-tracker
WORKDIR /java-tracker
RUN ./gradlew build
