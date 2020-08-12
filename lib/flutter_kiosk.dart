import 'dart:async';

import 'package:flutter/services.dart';

class FlutterKiosk {
  static const MethodChannel _channel = const MethodChannel('flutter_kiosk');

  static Future<bool> get isDeviceOwner async {
    return await _channel.invokeMethod('isDeviceOwner');
  }

  static Future<void> enableLockTask(bool enable) async {
    return await _channel.invokeMethod('enableLockTask', <String, dynamic>{
      'enable': enable,
    });
  }

  static Future<void> setKeyguardDisabled(bool disabled) async {
    return await _channel.invokeMethod('setKeyguardDisabled', <String, dynamic>{
      'disabled': disabled,
    });
  }

  static Future<void> addUserRestrictions(List<String> keys) async {
    return await _channel.invokeMethod('addUserRestrictions', <String, dynamic>{
      'keys': keys,
    });
  }

  static Future<void> clearUserRestrictions(List<String> keys) async {
    return await _channel
        .invokeMethod('clearUserRestrictions', <String, dynamic>{
      'keys': keys,
    });
  }

  static Future<void> installUpdate(String url) async {
    return await _channel.invokeMethod('installUpdate', <String, dynamic>{
      'url': url,
    });
  }
}