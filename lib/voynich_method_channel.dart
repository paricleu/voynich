import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'voynich_platform_interface.dart';

/// An implementation of [UntitledPlatform] that uses method channels.
class MethodChannelVoynich extends VoynichPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('untitled');

  @override
  Future<void> encryptSymmetric({
    required String inputPath,
    required String password,
    required String outputPath,
  }) async {
    return methodChannel.invokeMethod<void>(
      'encryptSymmetric',
      <String, dynamic>{
        'inputPath': inputPath,
        'password': password,
        'outputPath': outputPath,
      },
    );
  }

  @override
  Future<void> decryptSymmetric({
    required String inputPath,
    required String password,
    required String outputPath,
  }) async {
    return methodChannel.invokeMethod<void>(
      'decryptSymmetric',
      <String, dynamic>{
        'inputPath': inputPath,
        'password': password,
        'outputPath': outputPath,
      },
    );
  }
}
