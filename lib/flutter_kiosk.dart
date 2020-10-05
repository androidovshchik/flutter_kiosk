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

  static Future<void> startLockTask() async {
    return await _channel.invokeMethod('startLockTask');
  }

  static Future<void> stopLockTask() async {
    return await _channel.invokeMethod('stopLockTask');
  }

  static Future<void> toggleLockTask(bool enable) async {
    return await _channel.invokeMethod('toggleLockTask', <String, dynamic>{
      'enable': enable,
    });
  }

  static Future<void> setGlobalSetting(String setting, String value) async {
    return await _channel.invokeMethod('setGlobalSetting', <String, dynamic>{
      'setting': setting,
      'value': value,
    });
  }

  static Future<void> setKeyguardDisabled(bool disabled) async {
    return await _channel.invokeMethod('setKeyguardDisabled', <String, dynamic>{
      'disabled': disabled,
    });
  }

  static Future<void> setStatusBarDisabled(bool disabled) async {
    return await _channel.invokeMethod('setStatusBarDisabled', <String, dynamic>{
      'disabled': disabled,
    });
  }

  // ignore: slash_for_doc_comments
  /**
    You still need to declare the intent filter in your app manifest file as shown in the following XML snippet:
    @see https://developer.android.com/work/dpc/dedicated-devices/cookbook#be_the_home_app
   */
  static Future<void> setPersistentPreferredActivity() async {
    return await _channel.invokeMethod('setPersistentPreferredActivity');
  }

  static Future<void> clearPersistentPreferredActivities() async {
    return await _channel.invokeMethod('clearPersistentPreferredActivities');
  }

  static Future<void> addUserRestrictions(List<String> keys) async {
    return await _channel.invokeMethod('addUserRestrictions', <String, dynamic>{
      'keys': keys,
    });
  }

  static Future<void> clearUserRestrictions(List<String> keys) async {
    return await _channel.invokeMethod('clearUserRestrictions', <String, dynamic>{
      'keys': keys,
    });
  }

  static Future<void> clearDeviceOwner() async {
    return await _channel.invokeMethod('clearDeviceOwner');
  }

  /// Do not call this in production XD
  static Future<void> throwError() async {
    return await _channel.invokeMethod('throwError');
  }
}