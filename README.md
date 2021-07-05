# ReLF Client

The Android client for **ReLF: Scalable Remote Live Forensics for Android**.

## Prerequisites

- A running [ReLF server](https://github.com/nexus-lab/relf-server)

- Docker engine, or all of the following

    - Android Studio 4.2

    - Android SDK Platform 28 (install from SDK Manager in Android Studio)

    - Lombok plugin (install from File > Settings > Plugins in Android Studio)

    - Enable Annotation Processing in Android Studio (install from File > Other Settings >
      Settings for New Projects > Build, Execution, Deployment > Compiler > Annotation Processors)

## Download

```shell
git clone --recurse-submodules https://github.com/nexus-lab/relf-client.git
```

## Build

1. Create a client configuration file by copying `mobile/src/main/assets/config.example.yaml` to
   `mobile/src/main/assets/config.yaml`.

2. Go to your ReLF server and find your CA certificate in the `Settings` page.
   Copy it and set it as the value of `CA.certificate` in your client configuration file.
   Make sure you replace the spaces with new lines and keep the new lines in YAML.

   ```yaml
   CA.certificate: |
      -----BEGIN CERTIFICATE-----
      MIIC2zCCAcOgAwIBAgIBATANBgkqhkiG9w0BAQsFADAgMREwDwYDVQQDDAhncnJf
      dGVzdDELMAkGA1UEBhMCVVMwHhcNMjEwNzAzMTk1ODA3WhcNMzEwNzAyMTk1ODA3
      ...
      QcLM3O9rDkcLuiPZ4T4gDAV1JyVEh9WaVDGXaVH75dr4SZp/TZmtDcPrnH9ArH7e
      bXWIcw1C6fJpZxjCCxfz
      -----END CERTIFICATE-----
   ```

3. Set your ReLF server's public address to `Client.server_urls` in your client configuration file.
   The port number should be 8080 or your custom frontend port number.

   ```yaml
   Client.server_urls: http://<server_public_ip>:8080
   ```

4. Build the ReLF client.

   - If you are using Android Studio:
   
     1. Clean previous build by selecting "Build -> Clean Project".

     2. If you are building the client as a system privileged app, choose "systemDebug" as build 
        variant for module "relf-client.mobile" in the Build Variants panel.
        If you do so, you are also required to build the custom Android system image.
        Refer to [relf-aosp-vendor](https://github.com/nexus-lab/relf-aosp-vendor) for details.
        Otherwise, select "userDebug".

     3. Build APK by selecting "Build -> Build Bundle(s)/APK(s) -> Build APK(s)".

     4. You can find the built APK under `mobile/build/outputs/apk/<user or system>/debug` once
        the build is completed.

   - If you are using Docker:

     1. You may list all available Gradle tasks using

        ```shell
        docker run \
            --name relf-client-builder \
            --rm -it \
            -v $(pwd):/app \
            ghcr.io/nexus-lab/relf-client-builder \
            ./gradlew tasks
        ```

     2. To build the client as a system privileged app, using the following command.
        If you do so, you are also required to build the custom Android system image.
        Refer to [relf-aosp-vendor](https://github.com/nexus-lab/relf-aosp-vendor) for details.
        Otherwise, replace "assembleSystem" with "assembleUser" in the following command.

        ```shell
        docker run \
            --name relf-client-builder \
            --rm -it \
            -v $(pwd):/app \
            ghcr.io/nexus-lab/relf-client-builder \
            ./gradlew assembleSystem
        ```

     3. You can find the built APK under the subdirectories of 
        `mobile/build/outputs/apk/<user or system>/debug` once the build is completed.

## Run

You can now install ReLF client to a connected Android emulator or a physical device.
Make sure you grant all permissions to the app on your first run.
If your client configurations are correct, you should find the client enrolled in the admin 
dashboard of the ReLF server.
