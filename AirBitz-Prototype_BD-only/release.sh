

#!/bin/bash
 
export ORG_GRADLE_PROJECT_storeFile="$(pwd)/misc/airbitzbd-keystore.jks"
export ORG_GRADLE_PROJECT_storePassword="airb1tzBd"
export ORG_GRADLE_PROJECT_keyAlias="airbitzbd"
export ORG_GRADLE_PROJECT_keyPassword="airb1tzBd"
 
./gradlew signingReport
 
read -p "Is this correct? [y/n] " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./gradlew build

    # Create apks directory if it doesn't exist.
    if [ ! -d apks ]; then
        echo "Creating apks/ directory.";
        mkdir apks;
    fi;

    cp airbitz/build/apk/*.apk apks/
    rm apks/*-release-unaligned.apk
    rm apks/*-debug-*.apk
 
fi
