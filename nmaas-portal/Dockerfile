FROM alpine:latest as builder

COPY nmaas-portal /build/nmaas-portal
WORKDIR /build/nmaas-portal

RUN apk add nodejs nodejs-npm
RUN npm install -g @angular/cli
RUN npm ci
RUN ng build --base-href / --deploy-url / --prod


FROM nginx:alpine
MAINTAINER nmaas@lists.geant.org

ARG webdir=/usr/share/nginx/html

RUN mkdir -p ${webdir}/config
RUN apk add gettext

COPY --from=builder /build/nmaas-portal/build/app/ ${webdir}
COPY nmaas-portal/docker/nginx.conf /etc/nginx/nginx.conf
COPY nmaas-portal/docker/config.template.json ${webdir}/config/config.template.json
COPY nmaas-portal/docker/run_portal.sh /scripts/run_portal.sh

CMD /scripts/run_portal.sh && tail -f /dev/null
