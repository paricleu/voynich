import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class Voynich {
  static const MethodChannel _channel = const MethodChannel('voynich');

  static Future<void> encryptSymmetric(
      File input, String password, File output) async {
    return await _channel.invokeMethod<void>(
      'encryptSymmetric',
      <String, dynamic>{
        'inputPath': input.path,
        'password': password,
        'outputPath': output.path
      },
    );
  }

  static Future<void> decryptSymmetric(
      File input, String password, File output) async {
    return await _channel.invokeMethod<void>(
      'decryptSymmetric',
      <String, dynamic>{
        'inputPath': input.path,
        'password': password,
        'outputPath': output.path
      },
    );
  }
}
