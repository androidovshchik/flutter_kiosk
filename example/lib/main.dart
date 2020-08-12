import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_kiosk/flutter_kiosk.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _message;
  bool _executing;

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

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Kiosk app'),
        ),
        body: SingleChildScrollView(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(_message ?? 'No message',
                  textAlign: TextAlign.center, style: TextStyle(fontSize: 16)),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('isDeviceOwner', style: TextStyle(fontSize: 16)),
                onPressed: () {
                  execute(FlutterKiosk.isDeviceOwner);
                },
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