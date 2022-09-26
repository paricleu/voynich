import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'voynich_method_channel.dart';

abstract class VoynichPlatform extends PlatformInterface {
  /// Constructs a UntitledPlatform.
  VoynichPlatform() : super(token: _token);

  static final Object _token = Object();

  static VoynichPlatform _instance = MethodChannelVoynich();

  /// The default instance of [VoynichPlatform] to use.
  ///
  /// Defaults to [MethodChannelUntitled].
  static VoynichPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VoynichPlatform] when
  /// they register themselves.
  static set instance(VoynichPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> encryptSymmetric({
    required String inputPath,
    required String password,
    required String outputPath,
  }) {
    throw UnimplementedError('encryptSymmetric() has not been implemented.');
  }

  Future<void> decryptSymmetric({
    required String inputPath,
    required String password,
    required String outputPath,
  }) {
    throw UnimplementedError('decryptSymmetric() has not been implemented.');
  }
}
