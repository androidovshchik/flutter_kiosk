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

  String _info = "No info";

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
              Text(_info,
                  textAlign: TextAlign.center, style: TextStyle(fontSize: 16)),
              SizedBox(height: 16),
              RaisedButton(
                child: Text('isDeviceOwner', style: TextStyle(fontSize: 16)),
                onPressed: isDeviceOwner,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> isDeviceOwner() async {
    String info;
    try {
      info = (await FlutterKiosk.isDeviceOwner).toString();
    } on PlatformException catch (error) {
      info = error?.message ?? '';
    }
    if (!mounted) return;
    setState(() {
      _info = info;
    });
  }
}