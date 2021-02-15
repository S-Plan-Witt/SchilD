FROM openjdk:15

WORKDIR /
COPY target/S-Plan_SchildConnnector-jar-with-dependencies.jar S-Plan_SchildConnnector-jar-with-dependencies.jar
ENV GUI="false"
CMD java -jar S-Plan_SchildConnnector-jar-with-dependencies.jar