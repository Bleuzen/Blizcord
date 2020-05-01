#!/bin/bash

# NOTE: to use this build script, you need to have Maven, Ant, Proguard and Launch4j installed.
# On Archlinux you can install them for example with yay:
# yay -S maven ant proguard launch4j

# As an alternative, you can use IntelliJ IDEA to build the jar. It comes with Maven and Ant. Build configs for it exist in this repo.


# Force to build with Java 8
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk

# Download dependencies with maven
mvn dependency:copy-dependencies

# Build with ant
ant exe
