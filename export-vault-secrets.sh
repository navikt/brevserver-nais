#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/vault/brevserver_ds_username;
then
    echo "Setting BREVSERVER_DS_USERNAME"
    export  BREVSERVER_DS_USERNAME=$(cat /var/run/secrets/nais.io/vault/brevserver_ds_username)
fi
if test -f /var/run/secrets/nais.io/vault/brevserver_ds_password;
then
    echo "Setting BREVSERVER_DS_PASSWORD"
    export  BREVSERVER_DS_PASSWORD=$(cat /var/run/secrets/nais.io/vault/brevserver_ds_password)
fi