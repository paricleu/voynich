package tabsvsspaces.voynich.encryption

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.InvalidParameterException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("ClassName")
internal object performEncrypt {

    @JvmSynthetic
    internal fun <T> invoke(input: T,
                            cipher: Cipher,
                            keySpec: SecretKeySpec,
                            outputFile: File): File {

        if (outputFile.exists() && outputFile.absolutePath != CryptConstants.DEF_ENCRYPTED_FILE_PATH) {
            when (input) {
                is InputStream -> input.close()
            }
            throw  FileAlreadyExistsException(outputFile) // erl.onFailure(CryptConstants.ERR_OUTPUT_FILE_EXISTS, FileAlreadyExistsException(outputFile))
        }

        val salt = ByteArray(CryptConstants.SALT_BYTES_LENGTH)
        CryptConstants.random.nextBytes(salt)

        val iv = ByteArray(cipher.blockSize)
        CryptConstants.random.nextBytes(iv)
        val ivParams = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams)

        when (input) {

            /* is ByteArray -> (iv.plus(salt).plus(cipher.doFinal(input)))
                     .handleSuccess(erl, outputFile, true)*/

            is FileInputStream -> {

                if (outputFile.exists()) {
                    outputFile.delete()
                }
                outputFile.createNewFile()

                val fos = outputFile.outputStream()

                try {
                    fos.write(iv)
                    fos.write(salt)
                } catch (e: IOException) {
                    fos.flush()
                    fos.close()
                    input.close()
                    outputFile.delete()
                    throw e // erl.onFailure(CryptConstants.ERR_CANNOT_WRITE, e)
                }

                val cos = CipherOutputStream(fos, cipher)

                try {
                    val size = input.channel.size()
                    val buffer = ByteArray(8192)
                    var bytesCopied: Long = 0
                    var read = input.read(buffer)

                    while (read > -1) {
                        cos.write(buffer, 0, read)
                        bytesCopied += read
                        //erl.onProgress(read, bytesCopied, size)
                        read = input.read(buffer)
                    }

                } catch (e: IOException) {
                    outputFile.delete()
                    throw  e // erl.onFailure(CryptConstants.ERR_CANNOT_WRITE, e)
                } finally {
                    cos.flush()
                    cos.close()
                    input.close()
                }
                return outputFile // erl.onSuccess(outputFile)
            }

            else -> throw  InvalidParameterException() // erl.onFailure(CryptConstants.ERR_INPUT_TYPE_NOT_SUPPORTED, InvalidParameterException())
        }
    }
}