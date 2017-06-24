VERSION="0.3.7"
CHECKSUM="4b5211e209b400b1335f91049210741d"

sudo sh -c "echo \"Package: blizcord
Version: $VERSION
Homepage: https://github.com/Bleuzen/Blizcord
Depends: openjdk-8-jre, wget
Architecture: all
Section: net
Priority: optional
Maintainer: Bleuzen <supgesu@gmail.com>
Description: Blizcord
 A Discord Bot
 .
 https://github.com/Bleuzen/Blizcord\" > blizcord/DEBIAN/control"

# Replace version in postinst
sed -i -e 's/var_pkgver/$VERSION/g' blizcord/DEBIAN/postinst
sed -i -e 's/var_md5sum/$CHECKSUM/g' blizcord/DEBIAN/postinst


# Generate md5sums
find . -type f ! -regex '.*.hg.*' ! -regex '.*?debian-binary.*' ! -regex '.*?DEBIAN.*' -printf '%P ' | xargs md5sum > DEBIAN/md5sums

# Build
sudo chown -R root:root blizcord/
sudo dpkg-deb --build blizcord/
sudo mv blizcord.deb blizcord-$VERSION.deb
