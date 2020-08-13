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
  static const String _WAIT = 'Please, wait...';

  String _message;

  @override
  void initState() {
    super.initState();
    Screen.keepOn(true);
  }

  void showMessage(String message) {
    if (mounted) {
      setState(() {
        _message = message;
      });
    }
  }

  void startLock() async {
    showMessage(_WAIT);
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.toggleLockTask(true);
        await FlutterKiosk.setKeyguardDisabled(true);
        await FlutterKiosk.setStatusBarDisabled(true);
        await FlutterKiosk.addUserRestrictions([
          UserManager.DISALLOW_FACTORY_RESET,
          UserManager.DISALLOW_SAFE_BOOT,
          UserManager.DISALLOW_ADD_USER,
        ]);
        await FlutterKiosk.startLockTask();
        showMessage('The lock is started');
      } else {
        showMessage('This app is not a device owner');
      }
    } on PlatformException catch (e) {
      showMessage(e?.message);
    }
  }

  void stopLock() async {
    showMessage(_WAIT);
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.stopLockTask();
        await FlutterKiosk.clearUserRestrictions([
          UserManager.DISALLOW_FACTORY_RESET,
          UserManager.DISALLOW_SAFE_BOOT,
          UserManager.DISALLOW_ADD_USER,
        ]);
        await FlutterKiosk.setStatusBarDisabled(false);
        await FlutterKiosk.setKeyguardDisabled(false);
        await FlutterKiosk.toggleLockTask(false);
        showMessage('The lock is stopped');
      } else {
        showMessage('This app is not a device owner');
      }
    } on PlatformException catch (e) {
      showMessage(e?.message);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Kiosk app'),
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
                onPressed: _message != _WAIT ? startLock : null,
              ),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('stopLock', style: TextStyle(fontSize: 16)),
                onPressed: _message != _WAIT ? stopLock : null,
              ),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('installUpdate', style: TextStyle(fontSize: 16)),
                onPressed: _message != _WAIT ? () {
                  showMessage(_WAIT);
                  FlutterKiosk.installUpdate(
                      'https://github.com/androidovshchik/flutter_kiosk/releases/download/apk/app-release.apk')
                      .catchError((e) {
                    showMessage(e?.message);
                  });
                } : null,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
