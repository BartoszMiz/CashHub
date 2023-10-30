#! /bin/sh
./gradlew jar
cd app
java -jar build/libs/app.jar
