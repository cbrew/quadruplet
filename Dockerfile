FROM openjdk:11
COPY target/parser-1.0-SNAPSHOT-jar-with-dependencies.jar parser-server.jar
CMD java -jar parser-server.jar