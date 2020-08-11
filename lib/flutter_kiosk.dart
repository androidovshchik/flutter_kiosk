import 'dart:async';

import 'package:flutter/services.dart';

class FlutterKiosk {
  static const MethodChannel _channel = const MethodChannel('flutter_kiosk');

  static Future<bool> get isDeviceOwner async {
    return await _channel.invokeMethod('isDeviceOwner');
  }
}
