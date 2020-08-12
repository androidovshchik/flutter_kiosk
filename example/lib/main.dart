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

  void showMessage(String message) {
    if (mounted) {
      setState(() {
        _message = message;
      });
    }
  }

  void startLock() async {
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.toggleLockTask(true);
        await FlutterKiosk.startLockTask();
      } else {
        showMessage('This app is not a device owner');
      }
    } on PlatformException catch (e) {
      showMessage(e?.message);
    }
  }

  void stopLock() async {
    try {
      if (await FlutterKiosk.isDeviceOwner) {
        await FlutterKiosk.stopLockTask();
        await FlutterKiosk.toggleLockTask(false);
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
                  FlutterKiosk.installUpdate(
                          'https://github.com/androidovshchik/flutter_kiosk/releases/download/apk/app-release.apk')
                      .catchError((e) {
                    showMessage(e?.message);
                  }).whenComplete(() {});
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
