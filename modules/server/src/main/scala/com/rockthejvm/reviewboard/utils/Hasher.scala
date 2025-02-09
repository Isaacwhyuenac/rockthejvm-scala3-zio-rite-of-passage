package com.rockthejvm.reviewboard.utils

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Hasher {
  private val PBDKF2_ALGORITHM: String = "PBKDF2WithHmacSHA256"
  private val PBDKF2_ITERATIONS: Int   = 1_000
  private val SALT_BYTE_LENGTH: Int    = 24
  private val HASH_BYTE_LENGTH: Int    = 24
  private val secretKeyFactory         = SecretKeyFactory.getInstance(PBDKF2_ALGORITHM)

  private def pbkdf2(message: Array[Char], salt: Array[Byte], iterations: Int, nBytes: Int): Array[Byte] = {
    val keySpec   = new PBEKeySpec(message, salt, iterations, nBytes * 8)
    val secretKey = secretKeyFactory.generateSecret(keySpec)
    secretKey.getEncoded
  }

  private def toHex(bytes: Array[Byte]): String =
    bytes.map(byte => f"${byte}%02x").mkString

  private def fromHex(hex: String): Array[Byte] = {
//    val bytes = new Array[Byte](hex.length / 2)
//    for (i <- 0 until bytes.length) {
//      bytes(i) = Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16).toByte
//    }
//    bytes

    hex.sliding(2, 2).toArray.map(pair => Integer.parseInt(pair, 16).toByte)
  }

  // string + salted + nIteration PBKDF2
  def generateHash(password: String): String = {
    val rng  = new SecureRandom()
    val salt = Array.ofDim[Byte](SALT_BYTE_LENGTH)

    rng.nextBytes(salt) // generate 24 random bytes
    val hashBytes = pbkdf2(password.toCharArray, salt, PBDKF2_ITERATIONS, HASH_BYTE_LENGTH)

    s"${PBDKF2_ITERATIONS}:${toHex(salt)}:${toHex(hashBytes)}"
  }

  def validateHash(password: String, hash: String): Boolean = {
    val parts      = hash.split(":")
    val iterations = parts(0).toInt
    val salt       = fromHex(parts(1))
    val hashBytes  = fromHex(parts(2))

    val computedHash = pbkdf2(password.toCharArray, salt, iterations, HASH_BYTE_LENGTH)
    computedHash.sameElements(hashBytes)
  }

}
