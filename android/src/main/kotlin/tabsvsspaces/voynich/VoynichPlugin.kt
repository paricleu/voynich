package tabsvsspaces.voynich

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import tabsvsspaces.voynich.encryption.CryptConstants
import tabsvsspaces.voynich.encryption.CryptSymmetric
import java.io.File
import javax.crypto.spec.SecretKeySpec

/** VoynichPlugin */
public class VoynichPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "voynich")
        channel.setMethodCallHandler(this)

        crypt = CryptSymmetric()
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {

        lateinit var crypt: CryptSymmetric

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "voynich")
            channel.setMethodCallHandler(VoynichPlugin())

            crypt = CryptSymmetric()
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "encrypt" -> {
                val path = call.argument<String>("path")
                val cryptKeyHex = call.argument<String>("cryptKeyHex")
                val outputFilePath = call.argument<String>("outputFilePath")
                val file = File(path)
                val outputFile = File(outputFilePath)

                crypt.encrypt(file, SecretKeySpec(cryptKeyHex?.hexToByteArray(), CryptConstants.SECRET_KEY_SPEC_ALGORITHM), outputFile)

                result.success(outputFile.absolutePath)
            }
            "decrypt" -> {
                val path = call.argument<String>("path")
                val cryptKeyHex = call.argument<String>("cryptKeyHex")
                val outputFilePath = call.argument<String>("outputFilePath")
                val file = File(path)
                val outputFile = File(outputFilePath)

                crypt.decrypt(file, SecretKeySpec(cryptKeyHex?.hexToByteArray(), CryptConstants.SECRET_KEY_SPEC_ALGORITHM), outputFile)

                result.success(outputFile.absolutePath)
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
