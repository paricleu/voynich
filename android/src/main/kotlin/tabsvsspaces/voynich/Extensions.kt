package tabsvsspaces.voynich

import android.util.Base64

@Throws(IllegalArgumentException::class)
fun String.asByteArray(): ByteArray = this.toByteArray(Charsets.UTF_8)

@Throws(IllegalArgumentException::class)
fun String.fromBase64(): ByteArray = Base64.decode(this.asByteArray(), Base64.URL_SAFE)