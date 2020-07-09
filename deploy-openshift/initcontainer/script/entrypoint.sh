#!/bin/sh

#mkdir /config # created when mounted with private key secret
#openssl genrsa -out /run/private_key.pem 2048 # mounted directly via the private key secret
#mkdir /config/keys # created when emptydir volume is mounted

openssl pkcs8 -topk8 -inform PEM -outform DER -in /config/private_key.pem -out /config/keys/private_key.der -nocrypt

openssl rsa -in /config/private_key.pem -pubout -outform DER -out /config/keys/public_key.der
