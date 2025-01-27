FROM eclipse-temurin:17-jdk-jammy AS builder

COPY . /build/
WORKDIR /build/

RUN chmod +x ./gradlew && ./gradlew -Dorg.gradle.daemon=false build

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer=nmaas@lists.geant.org

COPY --from=builder /build/build/libs/*.jar /nmaas/platform/
COPY docker/run_platform.sh /nmaas/scripts/run_platform.sh
COPY docker/nmaas-platform.properties.template /nmaas/platform/config/nmaas-platform.properties.template
COPY docker/logback.xml /nmaas/platform/config/logback.xml
COPY docker/do-ntp.sh /etc/periodic/hourly/do-ntp.sh
COPY docker/ssh-config /root/.ssh/config

RUN apk add gettext postgresql-client
RUN mkdir /nmaas/files

RUN chmod +x /nmaas/scripts/run_platform.sh
CMD /nmaas/scripts/run_platform.sh && tail -f /dev/null
