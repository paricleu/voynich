package tabsvsspaces.voynich

import android.util.Base64
import java.util.regex.Pattern

@Throws(IllegalArgumentException::class)
fun String.asByteArray(): ByteArray = this.toByteArray(Charsets.UTF_8)

@Throws(IllegalArgumentException::class)
fun String.fromBase64(): ByteArray = Base64.decode(this.asByteArray(), Base64.URL_SAFE)

@Throws(IllegalArgumentException::class)
fun String.hexToByteArray(): ByteArray {
    val trimmed = this.replace(" ", "")
    if (!trimmed.isValidHex()) throw IllegalArgumentException("Invalid hex string.")
    val data = ByteArray(trimmed.length / 2)
    var i = 0
    while (i < trimmed.length) {
        data[i / 2] = ((Character.digit(trimmed[i], 16) shl 4) +
                Character.digit(trimmed[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

private val pHex: Pattern = Pattern.compile("[0-9a-fA-F]+")
fun String.isValidHex(): Boolean = (this.length % 2 == 0 && pHex.matcher(this).matches())
