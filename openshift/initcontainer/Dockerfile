FROM alpine:latest

RUN apk update && apk add --no-cache openssl

COPY script/entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
