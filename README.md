airbitz-android-gui
=========================

### Setup dependencies repositories

1. Clone the core and the android repository.

    ```bash
    git clone https://github.com/Airbitz/airbitz-core.git
    git clone https://github.com/Airbitz/airbitz-android-gui.git
    ```

1. Build the core. This requires swig and ndk-build to be in your path.

    ```bash
    cd airbitz-android-gui
    ./mkabc
    ```

1.  In order to work with google maps and the Airbitz Business Directory, you
    need to create a resource with your API keys in it.

    ```bash
    cd airbitz-android-gui
    cat <<EOF > ./airbitz/src/prod/res/values/all_keys.xml
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="google_maps_api_key">GOOGLE_KEY_HERE</string>
        <string name="airbitz_business_directory_key">AIRBITZ_BD_KEY_HERE</string>
        <string name="hockey_key">HOCKEY_APP_KEY_HERE</string>
    </resources>
    EOF
    ```

### Build with Android studio

1. Download Android Studio: http://developer.android.com/sdk/installing/studio.html
1. Open Image and drag to application folder.
1. Download Github for Mac and install.
1. Open and sign into Github, on the left side click on Airbitz.  Click on the button 'Clone to Computer' for the repository 'Airbitz/airbitz-wallet-ui-android' and select where you want to save.
1. Open Android Studio.  It might ask for a Java Runtime Environment which you can get here: http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html
1. Select Import Project, navigate to where you pulled the repository from Github, select 'Airbitz-Prototype_BD-Only' and hit ok.
1. Click through the next pages, making sure that the Gradle Environment and wrapper is selected if it pops up. It has been built with Gradle and so shouldn't need it.
1. Sometime during the installation/opening of Android Studio, the SDK manager should open, if it doesn't once you have Android Studio open, go to Tools->Android->SDK Manager.
1. In the SDK manager you will select the packages you need to install

   Currently they are: 
   Tools->Android SDK Tools
   Tools->Android SDK Platform-tools
   Tools->Android SDK Build-tools Rev  19.1
   Tools->Android SDK Build-tools Rev  19.0.3
   Tools->Android SDK Build-tools Rev  19.0.1
   Android 4.4.2 (API 19)->Android 4.4.2 (API 19)->SDK Platform
   Android 4.4.2 (API 19)->Google APIs (x86 System Image)
   Android 4.4.2 (API 19)->Google APIs (ARM System Image)
   Android 4.1.2 (API 16)->SDK PLatform
   Android 4.1.2 (API 16)->Google APIs
   Extras->Android Support Repository
   Extras->Android Support Library
   Extras->Google Play Services
   Extras->Google Repository

   Some of the Extras might not be available at first, install the others and they should be there.
    
1. To run Airbitz, click the solid green arrow on the toolbar at the top of the
   window. It might take a second but it will pop up a window in which you can
   select what to run it on.  If you have devices plugged in via USB they will
   show up here, if not you can create an emulator from the popup but I would
   recommend not doing this if possible as it takes forever to load, and is
   slow and difficult to interact with. Running on a physical Android device is
   much better.

### Build and install with gradle

    cd airbitz-android-gui/Airbitz
    ./gradlew installProdDebug
