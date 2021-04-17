#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/brevserverDS/username;
then
    echo "Setting BREVSERVER_DS_USERNAME"
    export  BREVSERVER_DS_USERNAME=$(cat /var/run/secrets/nais.io/brevserverDS/username)
fi
if test -f /var/run/secrets/nais.io/brevserverDS/password;
then
    echo "Setting BREVSERVER_DS_PASSWORD"
    export  BREVSERVER_DS_PASSWORD=$(cat /var/run/secrets/nais.io/brevserverDS/password)
fi
if test -f /var/run/secrets/nais.io/brevserverDS/url;
then
    echo "Setting BREVSERVER_DS_URL"
    export  BREVSERVER_DS_URL=$(cat /var/run/secrets/nais.io/brevserverDS/url)
fi