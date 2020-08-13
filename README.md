# flutter_kiosk

A Flutter plugin for kiosk mode in Android

Scan qr code to install example as device owner:

<img width="300" src="qr-code.gif"/>

## Getting Started

To set device owner with command line:

    $ adb shell dpm set-device-owner androidovshchik.flutter_kiosk_example/androidovshchik.flutter_kiosk.AdminReceiver

To get the checksum of apk:

    $ apksigner verify -print-certs *.apk | grep -Po "(?<=SHA-256 digest:) .*" | xxd -r -p | openssl base64 | tr -- '+/' '-_'