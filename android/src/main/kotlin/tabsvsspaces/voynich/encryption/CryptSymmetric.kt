package tabsvsspaces.voynich.encryption

import org.jetbrains.annotations.NotNull
import tabsvsspaces.voynich.asByteArray
import tabsvsspaces.voynich.fromBase64
import java.io.ByteArrayInputStream
import java.io.File
import java.security.InvalidParameterException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


/**
 * Secure symmetric encryption with AES256.
 */
class CryptSymmetric(transformation: CryptSymmetricTransformations
                     = CryptSymmetricTransformations.AES_CBC_PKCS7Padding) {

    private val cipher = Cipher.getInstance(transformation.value)

    fun generateKey(password: String = genSecureRandomPassword()): SecretKeySpec {
        val salt = ByteArray(CryptConstants.SALT_BYTES_LENGTH)
        CryptConstants.random.nextBytes(salt)

        val pbeKeySpec = PBEKeySpec(password.trim().toCharArray(), salt, CryptConstants.ITERATIONS, CryptConstants.KEY_BITS_LENGTH)
        val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(CryptConstants.SECRET_KEY_FAC_ALGORITHM)
        val keyBytes: ByteArray = keyFactory.generateSecret(pbeKeySpec).encoded
        return SecretKeySpec(keyBytes, CryptConstants.SECRET_KEY_SPEC_ALGORITHM)
    }

    @Throws(InvalidParameterException::class)
    @JvmOverloads
    fun genSecureRandomPassword(@NotNull length: Int = CryptConstants.PASSWORD_LENGTH,
                                @NotNull symbols: CharArray =
                                        CryptConstants.STANDARD_SYMBOLS.toCharArray()): String {

        if (length < 1 || length > 4096) throw InvalidParameterException(
                "Invalid length. Valid range is 1 to 4096.")

        if (symbols.isEmpty()) throw InvalidParameterException(
                "Array of symbols cannot be empty.")

        val password = CharArray(length)
        for (i in 0 until length) {
            password[i] = symbols[CryptConstants.random.nextInt(symbols.size - 1)]
        }
        return password.joinToString("")
    }


    /**
     * Symmetrically encrypts the input data using AES algorithm in CBC mode with PKCS7Padding padding
     *
     * Result can be a String or a File depending on the data type of [input] and parameter [outputFile].
     *
     * @param T which can be either of [String], [CharSequence],
     * [ByteArray], [InputStream], [FileInputStream], or [File]
     * @param input data to be encrypted
     * @param password string used to encrypt input
     * @param outputFile optional output file. If provided, result will be written to this file
     *
     * @exception InvalidKeyException if password is null or blank
     * @exception NoSuchFileException if input is a File which does not exists or is a Directory
     * @exception InvalidParameterException if input data type is not supported
     * @exception IOException if cannot read or write to a file
     * @exception FileAlreadyExistsException if output file is provided and already exists
     * @exception IllegalBlockSizeException if this cipher is a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size; or if this encryption algorithm is unable to
     * process the input data provided.
     */
    @JvmOverloads
    fun <T> encrypt(@NotNull input: T,
                    @NotNull keySpec: SecretKeySpec,
                    @NotNull outputFile: File = File(CryptConstants.DEF_ENCRYPTED_FILE_PATH)): File {

        when (input) {
            is String -> return encrypt(input.asByteArray(), keySpec, outputFile)

            is CharSequence ->
                return encrypt(input.toString().asByteArray(), keySpec, outputFile)

            is ByteArrayInputStream -> return encrypt(input.readBytes(), keySpec, outputFile)

            is File -> {
                if (!input.exists() || input.isDirectory) {
                    throw NoSuchFileException(input) // erl.onFailure(CryptConstants.ERR_NO_SUCH_FILE, NoSuchFileException(input))
                }
                val encryptedFile =
                        if (outputFile.absolutePath == CryptConstants.DEF_ENCRYPTED_FILE_PATH)
                            File(input.absolutePath + CryptConstants.ECRYPT_FILE_EXT)
                        else outputFile
                return encrypt(input.inputStream(), keySpec, encryptedFile)
            }

            else -> return performEncrypt.invoke(input, cipher, keySpec, outputFile)
        }
    }

    /**
     * Symmetrically decrypts the input data using AES algorithm in CBC mode with PKCS7Padding padding
     *
     * Result can be a String or a File depending on the data type of [input] and parameter [outputFile]
     *
     * @param input input data to be decrypted. It can be of type
     * [String], [CharSequence], [ByteArray], [InputStream], [FileInputStream], or [File]
     * @param password password string used to performEncrypt input
     * @param outputFile optional output file. If provided, result will be written to this file
     *
     * @exception InvalidKeyException if password is null or blank
     * @exception NoSuchFileException if input is a File which does not exists or is a Directory
     * @exception InvalidParameterException if input data type is not supported
     * @exception IOException if cannot read or write to a file
     * @exception FileAlreadyExistsException if output file is provided and already exists
     * @exception IllegalArgumentException if input data is not in valid format
     * @exception IllegalBlockSizeException if this cipher is a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size; or if this encryption algorithm is unable to
     * process the input data provided.
     * @exception BadPaddingException if this cipher is in decryption mode,
     * and (un)padding has been requested, but the decrypted data is not
     * bounded by the appropriate padding bytes
     */
    @JvmOverloads
    fun <T> decrypt(@NotNull input: T,
                    @NotNull keySpec: SecretKeySpec,
                    @NotNull outputFile: File = File(CryptConstants.DEF_DECRYPTED_FILE_PATH)) {

        when (input) {

            is String -> {
                try {
                    decrypt(input.fromBase64().inputStream(), keySpec, outputFile)
                } catch (e: IllegalArgumentException) {
                    throw  e // erl.onFailure(CryptConstants.ERR_BAD_BASE64, e)
                }
            }

            is CharSequence -> {
                try {
                    decrypt(input.toString().fromBase64().inputStream(), keySpec, outputFile)
                } catch (e: IllegalArgumentException) {
                    throw e // erl.onFailure(CryptConstants.ERR_BAD_BASE64, e)
                }
            }

            is ByteArray -> decrypt(input.inputStream(), keySpec, outputFile)

            is File -> {

                if (!input.exists() || input.isDirectory) {
                    throw NoSuchFileException(input) // erl.onFailure(CryptConstants.ERR_NO_SUCH_FILE, NoSuchFileException(input))
                }

                val decryptedFile =
                        if (outputFile.absolutePath == CryptConstants.DEF_DECRYPTED_FILE_PATH) {
                            File(input.absoluteFile.toString().removeSuffix(CryptConstants.ECRYPT_FILE_EXT))
                        } else {
                            outputFile
                        }

                decrypt(input.inputStream(), keySpec, decryptedFile)
            }

            else -> performDecrypt.invoke(input, cipher, keySpec, outputFile)
        }
    }
}