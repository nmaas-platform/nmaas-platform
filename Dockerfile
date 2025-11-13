FROM eclipse-temurin:21.0.9_10-jdk-alpine AS builder

COPY . /build/
WORKDIR /build/

RUN chmod +x ./gradlew \
    && ./gradlew -Dorg.gradle.daemon=false build -x test

FROM eclipse-temurin:21.0.9_10-jre-alpine

LABEL maintainer=nmaas@lists.geant.org

ARG USERNAME=nmaas
ARG USER_UID=1000
ARG USER_GID=1000
# Note: Latest version of kubectl may be found at https://github.com/kubernetes/kubernetes/releases
# renovate: datasource=github-releases depName=kubernetes/kubernetes
ENV KUBE_LATEST_VERSION=v1.16.3
# Note: Latest version of helm may be found at https://github.com/kubernetes/helm/releases
# renovate: datasource=github-releases depName=helm/helm
ENV HELM_VERSION=v3.18.4

COPY --from=builder /build/build/libs/*.jar /nmaas/platform/
COPY docker/docker_entrypoint.sh /nmaas/scripts/docker_entrypoint.sh
COPY docker/logback.xml /nmaas/platform/config/logback.xml
COPY docker/do-ntp.sh /etc/periodic/hourly/do-ntp.sh

RUN addgroup -g $USER_GID $USERNAME \
    && adduser --disabled-password -u $USER_UID -G $USERNAME $USERNAME

RUN apk --no-cache add gettext postgresql-client \
    && chmod +x /nmaas/scripts/docker_entrypoint.sh \
    && wget -q https://dl.k8s.io/release/${KUBE_LATEST_VERSION}/bin/linux/amd64/kubectl -O /usr/local/bin/kubectl \
    && chmod +x /usr/local/bin/kubectl \
    && wget -q https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz -O - | tar -xzO linux-amd64/helm > /usr/local/bin/helm \
    && chmod +x /usr/local/bin/helm

RUN mkdir -p /nmaas/files && chown -R $USERNAME:$USERNAME /nmaas/files \
    && chown -R $USERNAME:$USERNAME /nmaas/platform

USER $USERNAME

ENTRYPOINT /nmaas/scripts/docker_entrypoint.sh