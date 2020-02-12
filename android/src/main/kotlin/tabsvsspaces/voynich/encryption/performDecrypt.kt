package tabsvsspaces.voynich.encryption

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.InvalidParameterException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("ClassName", "LocalVariableName")
internal object performDecrypt {

    @JvmSynthetic
    internal fun <T> invoke(input: T,
                            cipher: Cipher,
                            keySpec: SecretKeySpec,
                            outputFile: File): File {

        if (outputFile.exists() && outputFile.absolutePath != CryptConstants.DEF_DECRYPTED_FILE_PATH) {
            when (input) {
                is InputStream -> input.close()
            }
            throw FileAlreadyExistsException(outputFile) // erl.onFailure(CryptConstants.ERR_OUTPUT_FILE_EXISTS, FileAlreadyExistsException(outputFile))
        }

        val IV_BYTES_LENGTH = cipher.blockSize

        when (input) {

            /* is ByteArrayInputStream -> {

                 val IV = ByteArray(IV_BYTES_LENGTH)
                 val salt = ByteArray(CryptConstants.SALT_BYTES_LENGTH)

                 if (IV_BYTES_LENGTH != input.read(IV) || CryptConstants.SALT_BYTES_LENGTH != input.read(salt)) {
                     input.close()
                     throw BadPaddingException() // erl.onFailure(CryptConstants.ERR_INVALID_INPUT_DATA, BadPaddingException())
                 }

                 val ivParams = IvParameterSpec(IV)
                 val key = getKey(password, salt)

                 try {
                     cipher.init(Cipher.DECRYPT_MODE, key, ivParams)

                     val secureBytes = input.readBytes()
                     cipher.doFinal(secureBytes).handleSuccess(erl, outputFile, false)

                 } catch (e: BadPaddingException) {
                     erl.onFailure(CryptConstants.ERR_INVALID_INPUT_DATA, e)
                 } catch (e: IllegalBlockSizeException) {
                     erl.onFailure(CryptConstants.ERR_INVALID_INPUT_DATA, e)
                 } finally {
                     input.close()
                 }
             }*/

            is FileInputStream -> {

                if (outputFile.exists()) {
                    outputFile.delete()
                }
                outputFile.createNewFile()

                var cis: CipherInputStream? = null
                val fos = outputFile.outputStream()

                val iv = ByteArray(IV_BYTES_LENGTH)
                val salt = ByteArray(CryptConstants.SALT_BYTES_LENGTH)

                try {
                    if (IV_BYTES_LENGTH != input.read(iv) || CryptConstants.SALT_BYTES_LENGTH != input.read(salt)) {
                        input.close()
                        throw BadPaddingException() // erl.onFailure(CryptConstants.ERR_INVALID_INPUT_DATA, BadPaddingException())
                    }
                } catch (e: IOException) {
                    input.close()
                    throw e //  erl.onFailure(CryptConstants.ERR_CANNOT_READ, e)
                }

                val ivParams = IvParameterSpec(iv)

                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams)

                try {
                    val size = input.channel.size()
                    cis = CipherInputStream(input, cipher)

                    val buffer = ByteArray(8192)
                    var bytesCopied: Long = 0

                    var read = cis.read(buffer)
                    while (read > -1) {
                        fos.write(buffer, 0, read)
                        bytesCopied += read
                        //erl.onProgress(read, bytesCopied, size)
                        read = cis.read(buffer)
                    }

                } catch (e: IOException) {
                    outputFile.delete()
                    throw e // erl.onFailure(CryptConstants.ERR_CANNOT_WRITE, e)
                } finally {
                    fos.flush()
                    fos.close()
                    cis?.close()
                }
                return outputFile
            }

            else -> throw  InvalidParameterException() // erl.onFailure(CryptConstants.ERR_INPUT_TYPE_NOT_SUPPORTED, InvalidParameterException())
        }
    }
}