#!/bin/bash
VERSION="0.3.7"
CHECKSUM="4b5211e209b400b1335f91049210741d"
BLIZCORD_INSTALL_DIR="$HOME/Blizcord/$VERSION"

if [ $(pidof -x "$0"| wc -w) -gt 2 ]; then 
    echo "Another instance is already running"
    exit
fi

BLIZCORD_BIN_FILE="$BLIZCORD_INSTALL_DIR/Blizcord.exe"
if [ ! -e "$BLIZCORD_BIN_FILE" ]; then
    mkdir -p $BLIZCORD_INSTALL_DIR

    BLIZCORD_DOWNLOAD_TEMP=$(mktemp --directory)
    cd $BLIZCORD_DOWNLOAD_TEMP

    echo "Downloading Blizcord ..."
    wget https://github.com/Bleuzen/Blizcord/releases/download/$VERSION/Blizcord.exe

    CHECKSUM_TEMP_FILE=$(mktemp)
    echo "$CHECKSUM  Blizcord.exe" > $CHECKSUM_TEMP_FILE
    if md5sum --status -c $CHECKSUM_TEMP_FILE; then
        echo "MD5 OK"
        mv Blizcord.exe $BLIZCORD_INSTALL_DIR
        echo "Successfully installed"
        echo
    else
        echo "Error: The MD5 sum didn't match"
        exit 1
    fi
fi

cd $BLIZCORD_INSTALL_DIR
java -jar Blizcord.exe "$@"
