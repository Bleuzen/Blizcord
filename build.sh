#!/bin/bash

# Build with Java 8
#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk

# Download dependencies with maven
mvn dependency:copy-dependencies

# Build with ant
ant exe
