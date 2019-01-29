FROM openjdk:8-jdk-slim as builder

COPY nmaas-platform /build/nmaas-platform
COPY .git /build/.git

WORKDIR /build/nmaas-platform/
RUN ./gradlew build -x test -x integrationTest

FROM openjdk:8-jre-alpine
MAINTAINER Michał Bień

VOLUME /tmp

COPY --from=builder /build/nmaas-platform/build/libs/*.jar /nmaas/platform/
COPY docker/platform/run_platform.sh /nmaas/scripts/run_platform.sh
COPY docker/platform/nmaas-platform.properties.template /nmaas/platform/config/nmaas-platform.properties.template
COPY docker/platform/do-ntp.sh /etc/periodic/hourly/do-ntp.sh
COPY docker/platform/ssh-config /root/.ssh/config

RUN apk add gettext

RUN mkdir /nmaas/files

CMD /nmaas/scripts/run_platform.sh && tail -f /dev/null
