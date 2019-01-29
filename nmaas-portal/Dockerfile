FROM alpine:latest as builder

COPY src /build/src
COPY angular.json /build/angular.json
COPY karma.conf.js /build/karma.conf.js
COPY package.json /build/package.json
COPY tslint.json /build/tslint.json
COPY protractor.conf.js /build/protractor.conf.js

WORKDIR /build

RUN apk add nodejs nodejs-npm
RUN npm install -g @angular/cli@^7.2.1
RUN npm install
RUN ng build --base-href / --deploy-url / --prod


FROM nginx:alpine
MAINTAINER Michał Bień

ARG webdir=/usr/share/nginx/html

RUN mkdir -p ${webdir}/config
RUN apk add gettext

COPY --from=builder /build/build/app/ ${webdir}
COPY ./docker/nginx.conf /etc/nginx/nginx.conf
COPY ./docker/config.template.json ${webdir}/config/config.template.json
COPY ./docker/run_portal.sh /scripts/run_portal.sh

CMD /scripts/run_portal.sh && tail -f /dev/null
