airbitz-android-gui
=========================

### Building

The Airbitz android application comes in 3 flavors. Production, Testnet and
Develop. To build it issue one of the following commands:

Develop version (Seperate App ID which does not conflict with production version. Also uses the develop branch of airbitz-core-java)

    ./gradlew installDevelopDebug

Testnet version

    ./gradlew installNettestDebug

Production version

    ./gradlew installProdDebug

#### Plugins

Airbitz includes [airbitz-plugins][plugins] which are HTML5 single page
applications that are included in the APK. If you have setup the
[airbitz-plugins][plugins] repository you can automatically build them with the
following gradle command.

    ./gradlew buildAirbitzPlugins

#### API Keys

In order to work with Airbitz servers, Google Maps, or any plugin partners
([glidera][glidera], [foldapp][foldapp], [clevercoin][clevercoin]), you'll need
API keys. Put your developer API key in the appropriate `keys.xml`

    cp src/prod/res/values/keys.xml.example    src/prod/res/values/keys.xml
    cp src/develop/res/values/keys.xml.example src/develop/res/values/keys.xml
    cp src/nettest/res/values/keys.xml.example src/nettest/res/values/keys.xml

The one required API to use the wallet is the `airbitz_api_key` which can be obtained from
https://developer.airbitz.co

Create an account using the Airbitz mobile app (https://airbitz.co/app) and use it to sign into
developer.airbitz.co by scanning the BitID barcode. Confirm your email and you'll then see an API
key in the account page. Use that for `airbitz_api_key`.

### Airbitz APIs

This app is build heavily on [airbitz-core-java][java-core] which leverages
[airbitz-core][core]. If you are interested in the underlying security,
and possibly using it in your application, please see our [API overview][library].

[plugins]: https://github.com/Airbitz/airbitz-plugins.git
[glidera]: https://glidera.io
[foldapp]: https://foldapp.com
[core]: https://github.com/Airbitz/airbitz-core.git
[java-core]: https://github.com/Airbitz/airbitz-core-java.git
[library]: https://airbitz.co/developer-api-library/
