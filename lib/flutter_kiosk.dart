import 'dart:async';

import 'package:flutter/services.dart';

class FlutterKiosk {
  static const MethodChannel _channel = const MethodChannel('flutter_kiosk');

  static Future<bool> get isDeviceOwner async {
    return await _channel.invokeMethod('isDeviceOwner');
  }

  static Future<void> installUpdate(String url) async {
    return await _channel.invokeMethod('installUpdate', <String, dynamic>{
      'url': url,
    });
  }
}
