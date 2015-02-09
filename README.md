airbitz-android-gui
=========================

### Setup dependencies repositories

1. Clone the core and the android repository.

    ```bash
    git clone https://github.com/Airbitz/airbitz-core.git
    git clone https://github.com/Airbitz/airbitz-android-gui.git
    ```
1. Download and install the Android NDK
https://developer.android.com/tools/sdk/ndk/index.html#download

1. Download and install Java JDK7 
https://jdk7.java.net/download.html

1. Build the core. This requires swig and ndk-build to be in your path.

    ```bash
    cd airbitz-android-gui
    ./mkabc
    ```

1.  In order to work with google maps and the Airbitz Business Directory, you
    need to create a resource with your API keys in it.

    ```bash
    cd airbitz-android-gui
    mkdir ./AirBitz/airbitz/src/debug
    mkdir ./AirBitz/airbitz/src/debug/res
    mkdir ./AirBitz/airbitz/src/debug/res/values
    
    cat <<EOF > ./AirBitz/airbitz/src/prod/res/values/all_keys.xml
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="google_maps_api_key">GOOGLE_KEY_HERE</string>
        <string name="airbitz_business_directory_key">AIRBITZ_BD_KEY_HERE</string>
        <string name="hockey_key">HOCKEY_APP_KEY_HERE</string>
    </resources>
    EOF
    
    cp ./AirBitz/airbitz/src/prod/res/values/all_keys.xml ./AirBitz/airbitz/src/debug/res/values/all_keys.xml
    ```

### Build with Android studio

1. Install Android Studio on your machine: http://developer.android.com/sdk/index.html
1. Download Github and install, or use Github from a browser.
1. Open and sign into Github, on the left side click on Airbitz.  Click on the button 'Clone to Computer' for the repository 'Airbitz/airbitz-android-gui' and select where you want to save.
1. Open Android Studio.  It might ask for a Java Runtime Environment which you can get here: http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html
1. Select Import Project, navigate to where you pulled the repository from Github, select 'Airbitz' and hit ok.
1. Click through the next pages, making sure that the Gradle Environment and wrapper is selected if it pops up. It has been built with Gradle and so shouldn't need it.
1. Sometime during the installation/opening of Android Studio, the SDK manager should open, if it doesn't once you have Android Studio open, go to Tools->Android->SDK Manager.
1. In the SDK manager you will select the packages you need to install

   Currently they are: 
   Tools->Android SDK Tools 24.0.2
   Tools->Android SDK Platform-tools 21
   Tools->Android SDK Build-tools Rev  21.1.1
   Android 5.0.1 (API 21)->SDK Platform
   Android 5.0.1 (API 21)->Google APIs
   Extras->Android Support Repository
   Extras->Android Support Library
   Extras->Google Play Services
   Extras->Google Repository
   Extras->Google USB Driver (if not Mac)

   Some of the Extras might not be available at first, install the others and they should be there.
    
1. To run Airbitz, click the solid green arrow on the toolbar at the top of the
   window. It might take a second but it will pop up a window in which you can
   select what to run it on.  If you have devices plugged in via USB they will
   show up here, if not you can create an emulator from the popup but I would
   recommend not doing this if possible as it takes forever to load, and is
   slow and difficult to interact with. Running on a physical Android device is
   much better.

### Build and install with gradle

```bash
cd airbitz-android-gui/Airbitz
./gradlew installProdDebug
```
