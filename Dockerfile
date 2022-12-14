FROM openjdk:11-jdk-slim as builder

COPY . /build/
WORKDIR /build/

RUN chmod +x ./gradlew
RUN ./gradlew build -x test -x integrationTest

FROM adoptopenjdk/openjdk11:jre-11.0.14.1_1-alpine
MAINTAINER nmaas@lists.geant.org

COPY --from=builder /build/build/libs/*.jar /nmaas/platform/
COPY docker/run_platform.sh /nmaas/scripts/run_platform.sh
COPY docker/nmaas-platform.properties.template /nmaas/platform/config/nmaas-platform.properties.template
COPY docker/log4j2-spring.json /nmaas/platform/config/log4j2-spring.json
COPY docker/do-ntp.sh /etc/periodic/hourly/do-ntp.sh
COPY docker/ssh-config /root/.ssh/config

RUN apk add gettext postgresql-client

RUN mkdir /nmaas/files

RUN chmod +x /nmaas/scripts/run_platform.sh
CMD /nmaas/scripts/run_platform.sh && tail -f /dev/null
