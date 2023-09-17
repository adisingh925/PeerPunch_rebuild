package app.adreal.android.peerpunch.encryption

import android.security.keystore.KeyProperties
import android.util.Log
import app.adreal.android.peerpunch.model.EncryptedData
import app.adreal.android.peerpunch.storage.SharedPreferences
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encryption {

    private var ECDH_PUBLIC = ""
    private var ECDH_PRIVATE = ""
    private const val ELLIPTIC_CURVE_ALGORITHM = "ECDH"
    private const val CURVE_NAME = "secp256r1"
    private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE_AES = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING_AES = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION_AES = "$AES_ALGORITHM/$BLOCK_MODE_AES/$PADDING_AES"
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private var SYMMETRIC_KEY = ""

    fun getECDHPublicKey(): String {
        return ECDH_PUBLIC
    }

    fun addBouncyCastleProvider() {
        Log.d("Encryption", "Generating KeyPair")
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())
        generateECDHKeyPair()
    }

    /**
     * This function will generate ECDH key pair
     */
    private fun generateECDHKeyPair() {
        val ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME)
        val keyGen = KeyPairGenerator.getInstance(ELLIPTIC_CURVE_ALGORITHM, BouncyCastleProvider())
        keyGen.initialize(ecSpec, SecureRandom())
        storeECDHKeyPair(keyGen.generateKeyPair())
    }

    private fun storeECDHKeyPair(keyPair: KeyPair) {
        ECDH_PUBLIC = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        ECDH_PRIVATE = Base64.getEncoder().encodeToString(keyPair.private.encoded)
    }

    /**
     * This function will use public key and private key to generate shared secret
     * i.e symmetric AES key
     */
    fun generateECDHSecret(publicSecret: String) {
        Log.d("Encryption", "Generating ECDH secret")
        val publicKey = getECDHPublicKeyFromBase64String(publicSecret)
        val privateKey = getECDHPrivateKeyFromBase64String()
        val sharedSecret = getECDHSharedSecret(publicKey, privateKey)
        val aesKey = getAESKeyFromSharedSecret(sharedSecret)
        SYMMETRIC_KEY = Base64.getEncoder().encodeToString(aesKey.encoded)
    }

    /**
     * It will convert base64 string back to public key from shared preferences
     */
    private fun getECDHPublicKeyFromBase64String(publicKeyBase64: String): ECPublicKey {
        val publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64)
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    /**
     * It will retrieve the private key from shared preferences using base64 string
     */
    private fun getECDHPrivateKeyFromBase64String(): ECPrivateKey {
        val privateSecret = ECDH_PRIVATE
        val priKey = Base64.getDecoder().decode(privateSecret)
        val keySpec = PKCS8EncodedKeySpec(priKey)
        val keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        return keyFactory.generatePrivate(keySpec) as ECPrivateKey
    }

    /**
     * This function take public key and private key to generate shared secret
     */
    private fun getECDHSharedSecret(publicKey: ECPublicKey, privateKey: ECPrivateKey): ByteArray {
        val keyAgreement = KeyAgreement.getInstance(ELLIPTIC_CURVE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME)
        keyAgreement.init(privateKey, SecureRandom())
        val keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        val ecPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKey.encoded)) as ECPublicKey
        keyAgreement.doPhase(ecPublicKey, true)
        return keyAgreement.generateSecret()
    }

    /**
     * This function will convert secret key to AES symmetric key
     */
    private fun getAESKeyFromSharedSecret(sharedSecret: ByteArray): SecretKeySpec {
        val hashBasedKeyDerivation = HKDFBytesGenerator(SHA256Digest())
        val derivedKey = ByteArray(32)
        hashBasedKeyDerivation.init(HKDFParameters(sharedSecret, null, null))
        hashBasedKeyDerivation.generateBytes(derivedKey, 0, 32)
        return SecretKeySpec(derivedKey, AES_ALGORITHM)
    }

    /**
     * This function will encrypt data using AES symmetric key
     */
    fun encryptUsingSymmetricKey(data: String): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES)
        cipher.init(Cipher.ENCRYPT_MODE, getStoredSymmetricEncryptionKey(), SecureRandom())
        val iv = cipher.iv
        val plaintext = data.toByteArray()
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedData(ciphertext, iv)
    }

    /**
     * This function will decrypt data using AES symmetric key
     */
    fun decryptUsingSymmetricEncryption(
        cipherText: ByteArray,
        iv: ByteArray
    ): String {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, getStoredSymmetricEncryptionKey(), ivParameterSpec)
        return cipher.doFinal(cipherText).decodeToString()
    }

    /**
     * This function will generate HMAC for the message
     */
    fun generateHMAC(message: String): String {
        try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(getStoredSymmetricEncryptionKey())
            val hash = mac.doFinal(message.toByteArray())
            return hash.joinToString("") { String.format("%02x", it) }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * This function will compare the message and HMAC
     */
    fun compareMessageAndHMAC(msg: String, hash: String): Boolean {
        return generateHMAC(msg) == hash
    }

    /**
     * This function will store the AES symmetric key in shared preferences
     */
    private fun getStoredSymmetricEncryptionKey(): SecretKeySpec {
        return SecretKeySpec(
            Base64.getDecoder().decode(SYMMETRIC_KEY), AES_ALGORITHM
        )
    }
}