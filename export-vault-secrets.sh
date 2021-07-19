#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvbrevserver/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export FAGARKIV_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvbrevserver/username)
fi

if test -f /var/run/secrets/nais.io/srvbrevserver/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export FAGARKIV_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvbrevserver/password)
fi