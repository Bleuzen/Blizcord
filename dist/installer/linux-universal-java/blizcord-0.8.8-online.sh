#!/bin/bash
VERSION_TAG="0.8.8"

BLIZCORD_INSTALL_DIR="$HOME/bin/Blizcord/$VERSION_TAG"
BLIZCORD_FILE_NAME="Blizcord-$VERSION_TAG.exe"
BLIZCORD_FILE_URL="https://github.com/Bleuzen/Blizcord/releases/download/$VERSION_TAG/$BLIZCORD_FILE_NAME"
BLIZCORD_SHA1_FILE_URL="$BLIZCORD_FILE_URL.sha1"

BLIZCORD_BIN_FILE="$BLIZCORD_INSTALL_DIR/$BLIZCORD_FILE_NAME"
if [ ! -e "$BLIZCORD_BIN_FILE" ]; then
    mkdir -p $BLIZCORD_INSTALL_DIR

    BLIZCORD_DOWNLOAD_TEMP=$(mktemp --directory)
    cd $BLIZCORD_DOWNLOAD_TEMP

    echo "Downloading Blizcord ..."
    wget $BLIZCORD_FILE_URL

    CHECKSUM_TEMP_FILE=$(mktemp)
    wget -O $CHECKSUM_TEMP_FILE $BLIZCORD_SHA1_FILE_URL
    if sha1sum --status -c $CHECKSUM_TEMP_FILE; then
        echo "SHA1 OK"
        mv Blizcord.exe $BLIZCORD_INSTALL_DIR
        echo "Successfully installed"
        echo
    else
        echo "Error: The SHA1 sum didn't match"
        exit 1
    fi
fi

cd $BLIZCORD_INSTALL_DIR
java -jar $BLIZCORD_FILE_NAME "$@"
