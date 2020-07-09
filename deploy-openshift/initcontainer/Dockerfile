# This dockerfile runs as an init container
# For converting the given pem file to pkcs8 format to be read by java

FROM alpine:latest

RUN apk update && apk add --no-cache openssl

COPY script/entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
