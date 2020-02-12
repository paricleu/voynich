package tabsvsspaces.voynich.encryption

/**
 * Transformations that can be used for symmetric encryption/decryption
 */
enum class CryptSymmetricTransformations(val value: String) {

    AES_CTR_NoPadding("AES/CTR/NoPadding"),
    AES_CBC_PKCS7Padding("AES/CBC/PKCS7Padding")

}