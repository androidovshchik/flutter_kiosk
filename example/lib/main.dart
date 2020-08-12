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
  String _info;

  void execute(Future<dynamic> future) async {
    String info;
    _info = info;
    try {
      info = (await future).toString();
    } on PlatformException catch (error) {
      info = error?.message;
    }
    if (!mounted) return;
    setState(() {
      _info = info;
    });
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
              Text(
                  _info ?? 'No info',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 16)
              ),
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
                  execute(FlutterKiosk.installUpdate(''));
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}