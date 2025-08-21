ARG CADDY_VERSION=2.8

FROM docker-registry.dvbern.ch/dockerhub/library/caddy:${CADDY_VERSION}-builder-alpine AS builder

# build unpriviledged Caddy (required for OpenShift)
# therefore you must listen on ports >= 1024 e.g. 8080 for HTTP in your Caddyfile
ENV XCADDY_SETCAP=0

RUN xcaddy build

FROM docker-registry.dvbern.ch/dockerhub/library/caddy:${CADDY_VERSION}-alpine

COPY --from=builder /usr/bin/caddy /usr/bin/caddy

ARG APP="starty-app"

ARG VERS_INFO
ENV APP_VERSION=$VERS_INFO

ENV ADDITIONAL_FRAME_ANCESTORS ''

COPY Caddyfile /etc/caddy/Caddyfile
COPY dist/apps/${APP}/browser /srv/app

RUN adduser -u 101 -D -S -G www-data www-data;
RUN chown -R www-data:www-data /config /srv/app /data

USER www-data
