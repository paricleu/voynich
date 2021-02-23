package tabsvsspaces.voynich

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import tabsvsspaces.voynich.encryption.CryptConstants
import tabsvsspaces.voynich.encryption.CryptSymmetric
import java.io.File
import javax.crypto.spec.SecretKeySpec

/** VoynichPlugin */
class VoynichPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private val crypt = CryptSymmetric()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "voynich")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "encryptSymmetric" -> {
                val inputPath = call.argument<String>("inputPath")
                val password = call.argument<String>("password")
                val outputPath = call.argument<String>("outputPath")
                val input = File(inputPath)
                val output = File(outputPath)

                crypt.encrypt(input, SecretKeySpec(password?.hexToByteArray(), CryptConstants.SECRET_KEY_SPEC_ALGORITHM), output)

                result.success(null)
            }
            "decryptSymmetric" -> {
                val inputPath = call.argument<String>("inputPath")
                val password = call.argument<String>("password")
                val outputPath = call.argument<String>("outputPath")
                val input = File(inputPath)
                val output = File(outputPath)

                crypt.decrypt(input, SecretKeySpec(password?.hexToByteArray(), CryptConstants.SECRET_KEY_SPEC_ALGORITHM), output)

                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
