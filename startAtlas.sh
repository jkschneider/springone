#!/usr/bin/env bash
mkdir -p dist

if [ ! -d dist/atlas-1.4.4-standalone.jar ]
then
    curl -Lo dist/atlas-1.4.4-standalone.jar 'https://github.com/Netflix/atlas/releases/download/v1.4.4/atlas-1.4.4-standalone.jar'
    curl -Lo dist/memory.conf 'https://raw.githubusercontent.com/Netflix/atlas/v1.4.x/conf/memory.conf'
fi

java -jar dist/atlas-1.4.4-standalone.jar dist/memory.conf