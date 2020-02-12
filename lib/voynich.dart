import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class Voynich {
  static const MethodChannel _channel = const MethodChannel('voynich');

  static Future<File> encrypt(File file, String cryptKeyHex, File output) async {
    assert(file != null);
    final String path = await _channel.invokeMethod<String>(
      'encrypt',
      <String, dynamic>{'path': file.path, 'cryptKeyHex': cryptKeyHex},
    );

    return path == null ? null : File(path);
  }

  static Future<File> decrypt(File file, String cryptKeyHex, File output) async {
    assert(file != null);
    final String path = await _channel.invokeMethod<String>(
      'decrypt',
      <String, dynamic>{'path': file.path, 'cryptKeyHex': cryptKeyHex, 'outputPath': output.path},
    );

    return path == null ? null : File(path);
  }
}
