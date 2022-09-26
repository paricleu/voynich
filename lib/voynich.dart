import 'dart:async';
import 'dart:io';

import 'package:voynich/voynich_platform_interface.dart';

class Voynich {
  Future<void> encryptSymmetric(
    File input,
    String password,
    File output,
  ) async {
    return VoynichPlatform.instance.encryptSymmetric(
      inputPath: input.path,
      password: password,
      outputPath: output.path,
    );
  }

  Future<void> decryptSymmetric(
    File input,
    String password,
    File output,
  ) async {
    return VoynichPlatform.instance.decryptSymmetric(
      inputPath: input.path,
      password: password,
      outputPath: output.path,
    );
  }
}
