# 1. Place Blizcord in opt/Blizcord

# 2. Change the version
VERSION="0.1.7"

sudo sh -c "echo \"Package: blizcord
Version: $VERSION
Homepage: https://github.com/Bleuzen/Blizcord
Depends: openjdk-8-jre
Architecture: all
Section: net
Priority: optional
Maintainer: Bleuzen <supgesu@gmail.com>
Description: Blizcord
 A Discord Bot
 .
 https://github.com/Bleuzen/Blizcord\" > blizcord/DEBIAN/control"

sudo chown -R root:root blizcord/
sudo dpkg-deb --build blizcord/
sudo mv blizcord.deb blizcord-$VERSION.deb
