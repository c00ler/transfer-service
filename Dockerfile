FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.4_11

ENV PRJDIR /usr/src/dev

COPY ./pom.xml $PRJDIR/pom.xml
COPY ./.mvn $PRJDIR/.mvn
COPY ./mvnw $PRJDIR/mvnw

WORKDIR $PRJDIR

RUN ./mvnw dependency:go-offline -B

COPY ./src $PRJDIR/src

RUN ./mvnw clean verify

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "./target/service-jar-with-dependencies.jar"]
