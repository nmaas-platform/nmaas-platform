FROM eclipse-temurin:17-jdk-jammy AS builder

COPY . /build/
WORKDIR /build/

RUN chmod +x ./gradlew \
    && ./gradlew -Dorg.gradle.daemon=false build

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer=nmaas@lists.geant.org

COPY --from=builder /build/build/libs/*.jar /nmaas/platform/
COPY docker/run_platform.sh /nmaas/scripts/run_platform.sh
COPY docker/logback.xml /nmaas/platform/config/logback.xml
COPY docker/do-ntp.sh /etc/periodic/hourly/do-ntp.sh
COPY docker/ssh-config /root/.ssh/config

RUN apk --no-cache add gettext postgresql-client \
    && mkdir /nmaas/files \
    && chmod +x /nmaas/scripts/run_platform.sh

CMD /nmaas/scripts/run_platform.sh && tail -f /dev/null