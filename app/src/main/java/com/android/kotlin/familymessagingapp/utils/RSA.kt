package com.android.kotlin.familymessagingapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

/**
 * RSA algorithm
 * @reference https://www.javatpoint.com/rsa-encryption-algorithm
 */
object RSA {
    // Function to generate RSA key pair
    data class RSAKeyPair(val publicKey: PublicKey, val privateKey: PrivateKey)
    data class PublicKey(val e: BigInteger, val n: BigInteger)
    data class PrivateKey(val d: BigInteger, val n: BigInteger)

    // Use SecureRandom instead of Random for cryptographic security
    private val random = SecureRandom()

    /**
     * RSA key pair generator
     * @return public and private key pairs
     */
    suspend fun generateRSAKeys(bitLength: Int = 2048): RSAKeyPair? {
        // Dispatchers.Default - This dispatcher is optimized to perform CPU-intensive work outside
        // of the main thread. Example use cases include sorting a list and parsing JSON.
        return withContext(Dispatchers.Default) {
            try {
                // Generate two large prime numbers p and q
                val p = BigInteger.probablePrime(bitLength / 2, random)
                val q = BigInteger.probablePrime(bitLength / 2, random)

                // Calculate n = p * q
                val n = p * q

                // Calculate phi(n) = (p-1) * (q-1)
                val phiN = (p - BigInteger.ONE) * (q - BigInteger.ONE)

                // Choose a small odd integer e, commonly 65537
                val e = BigInteger.valueOf(65537L)

                // Ensure gcd(e, phiN) == 1
                require(e.gcd(phiN) == BigInteger.ONE) { "e and phi(n) are not coprime" }

                // Calculate d, the modular inverse of e
                val d = e.modInverse(phiN)

                // Return the key pair
                RSAKeyPair(
                    PublicKey(e, n),
                    PrivateKey(d, n)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun encrypt(message: BigInteger, publicKey: PublicKey): BigInteger {
        val (e, n) = publicKey
        return message.modPow(e, n)
    }


    fun decrypt(ciphertext: BigInteger, privateKey: PrivateKey): BigInteger {
        val (d, n) = privateKey
        return ciphertext.modPow(d, n)
    }

    // Convert text from UTF-8 to BigInteger
    fun utf8ToBigInteger(text: String): BigInteger {
        return BigInteger(1, text.toByteArray(StandardCharsets.UTF_8))
    }

    // Convert text from BigInteger to UTF-8
    fun bigIntegerToUtf8(bigInteger: BigInteger): String {
        val byteArray = bigInteger.toByteArray().dropWhile { it == 0.toByte() }
            .toByteArray() // Avoid negative bytes
        return String(byteArray, StandardCharsets.UTF_8)
    }
}
