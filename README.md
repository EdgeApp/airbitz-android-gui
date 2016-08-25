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
API keys. They are placed in an xml file called keys.xml in
`src/prod/res/values` or `src/nettest/res/values` or `src/develop/res/values`.

### Airbitz APIs

This app is build heavily on [airbitz-core-java][java-core] which leverages
[airbitz-core][core]. If you are interested in the underlying security,
and possibly using it in your application, please see our [API overview][library].

[plugins]: https://github.com/Airbitz/airbitz-plugins.git
[glidera]: https://glidera.io 
[foldapp]: https://foldapp.com
[clevercoin]: https://corporate.clevercoin.com
[core]: https://github.com/Airbitz/airbitz-core.git
[java-core]: https://github.com/Airbitz/airbitz-core-java.git
[library]: https://airbitz.co/developer-api-library/
