#!/usr/bin/env bash

DOCKER_NAME='authenticator'
VERSION=$1

if hash sbt 2>/dev/null; then
    echo -e "\nAttempting compilation with existing SBT version\n"
else
    echo -e "\n\nRequesting superuser permissions to install SBT..."
    sudo echo -e "Granted!\n"

    wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.15.deb
    sudo dpkg -i sbt-0.13.15.deb
fi

sbt clean compile assembly

docker build -t ${DOCKER_NAME}:${VERSION} .

echo -e "\n\nDone!\n"