# flutter_kiosk

A Flutter plugin for kiosk mode in Android

## Getting Started

To get the checksum of apk:

    $ apksigner verify -print-certs *.apk | grep -Po "(?<=SHA-256 digest:) .*" | xxd -r -p | openssl base64 | tr -- '+/' '-_'