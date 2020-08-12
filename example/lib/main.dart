import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_kiosk/flutter_kiosk.dart';
import 'package:flutter_kiosk/user_manager.dart' as UserManager;
import 'package:screen/screen.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _message;
  bool _executing = false;

  @override
  void initState() {
    super.initState();
    Screen.keepOn(true);
    // todo remove
    UserManager.DISALLOW_ADD_MANAGED_PROFILE;
  }

  void execute(Future<dynamic> future) async {
    if (_executing) {
      return;
    }
    _executing = true;
    setState(() {
      _message = 'Please, wait...';
    });
    String message;
    try {
      message = (await future)?.toString();
    } on PlatformException catch (error) {
      message = error?.message;
    }
    if (!mounted) {
      return;
    }
    setState(() {
      _message = message;
    });
    _executing = false;
  }

  void startLock() async {
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.toggleLockTask(true);
        await FlutterKiosk.startLockTask();
      }
    } on PlatformException catch (error) {}
  }

  void stopLock() async {
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.stopLockTask();
        await FlutterKiosk.toggleLockTask(false);
      }
    } on PlatformException catch (error) {}
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Kiosk app', style: TextStyle(fontSize: 16)),
        ),
        body: Container(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(_message ?? 'No message',
                  textAlign: TextAlign.center, style: TextStyle(fontSize: 16)),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('startLock', style: TextStyle(fontSize: 16)),
                onPressed: startLock,
              ),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('stopLock', style: TextStyle(fontSize: 16)),
                onPressed: stopLock,
              ),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('installUpdate', style: TextStyle(fontSize: 16)),
                onPressed: () {
                  execute(FlutterKiosk.installUpdate(
                      'https://github.com/androidovshchik/flutter_kiosk/releases/download/apk/app-release.apk'));
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
