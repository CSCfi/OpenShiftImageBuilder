#!/bin/sh

openssl genrsa -out private_key.pem 2048

mkdir keys

openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out keys/private_key.der -nocrypt

openssl rsa -in private_key.pem -pubout -outform DER -out keys/public_key.der
