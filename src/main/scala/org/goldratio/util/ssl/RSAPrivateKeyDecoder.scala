package org.goldratio.util.ssl

/**
 * Created by GoldRatio on 4/9/14.
 */
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

import java.math.BigInteger

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.RSAPrivateCrtKeySpec

class RSAPrivateKeyDecoder(is: InputStream) {

  /**
   * Constructs ...
   *
   *
   * @param bytes
   */
  def this(bytes: Array[Byte]) = this(new ByteArrayInputStream(bytes))


  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  def getKeySpec(): RSAPrivateCrtKeySpec  = {
    // Skip to the beginning of the sequence:
    val tag = is.read()
    val len = readLength()

    // System.out.println("Sequence: " + tag + ", size: " + len)
    val ver = nextInt()
    val mod = nextInt()
    val pubExp = nextInt()
    val privExp = nextInt()
    val prime1 = nextInt()
    val prime2 = nextInt()
    val exp1 = nextInt()
    val exp2 = nextInt()
    val coef = nextInt()

    return new RSAPrivateCrtKeySpec(mod, pubExp, privExp, prime1, prime2, exp1, exp2, coef)
  }

  /**
   * Method description
   *
   *
   * @return
   *
   *
   * @throws IOException
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   */
  def getPrivateKey(): PrivateKey = {
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(getKeySpec())
  }

  //~--- methods --------------------------------------------------------------

  def nextInt(): BigInteger = {
    val tag = is.read()
    val len = readLength()
    val value = new Array[Byte](len)
    val res = is.read(value)

    if (res < len) {
      throw new IOException("Invalid DER data: data too short.")
    }

    new BigInteger(value)
  }

  def readLength(): Int = {
    val len = is.read()

    if (len == -1) {
      throw new IOException("Invalid field length in DER data.")
    }

    if ((len & ~0x7F) == 0) {
      len
    }
    else {
      val size = len & 0x7F

      if ((len >= 0xFF) || (size > 4)) {
        throw new IOException("Invalid field length in DER data: too big (" + len + ")")
      }

      val bytes = new Array[Byte](size)
      val res = is.read(bytes)

      if (res < size) {
        throw new IOException("Invalid DER file: data too short.")
      }
      new BigInteger(1, bytes).intValue()
    }


  }
}
