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

  String _info = "";

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

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Kiosk app'),
        ),
        body: SingleChildScrollView(
          child: Column(
            children: [
              Text(_info),
              RaisedButton(
                child: Text('isDeviceOwner'),
                onPressed: isDeviceOwner,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
