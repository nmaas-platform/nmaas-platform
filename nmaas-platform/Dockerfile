FROM openjdk:8-jdk-slim as builder

COPY nmaas-platform /build/nmaas-platform
COPY .git /build/.git

WORKDIR /build/nmaas-platform/
RUN ./gradlew build -x test -x integrationTest

FROM openjdk:8-jre-alpine
MAINTAINER nmaas@lists.geant.org

VOLUME /tmp

COPY --from=builder /build/nmaas-platform/build/libs/*.jar /nmaas/platform/
COPY nmaas-platform/docker/run_platform.sh /nmaas/scripts/run_platform.sh
COPY nmaas-platform/docker/nmaas-platform.properties.template /nmaas/platform/config/nmaas-platform.properties.template
COPY nmaas-platform/docker/log4j2-spring.json /nmaas/platform/config/log4j2-spring.json
COPY nmaas-platform/docker/do-ntp.sh /etc/periodic/hourly/do-ntp.sh
COPY nmaas-platform/docker/ssh-config /root/.ssh/config

RUN apk add gettext postgresql-client

RUN mkdir /nmaas/files

CMD /nmaas/scripts/run_platform.sh && tail -f /dev/null
