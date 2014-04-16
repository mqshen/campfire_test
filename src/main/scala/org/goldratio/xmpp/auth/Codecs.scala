package org.goldratio.xmpp.auth

import sun.misc.{BASE64Decoder, BASE64Encoder}

/**
 * Created by GoldRatio on 4/9/14.
 */
object Codec {

  /**
   * Computes the SHA-1 digest for a byte array.
   *
   * @param bytes the data to hash
   * @return the SHA-1 digest, encoded as a hex string
   */
  def sha1(bytes: Array[Byte]): String = {
    import java.security.MessageDigest
    val digest = MessageDigest.getInstance("SHA-1")
    digest.reset()
    digest.update(bytes)
    digest.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }

  /**
   * Computes the MD5 digest for a byte array.
   *
   * @param bytes the data to hash
   * @return the MD5 digest, encoded as a hex string
   */
  def md5(bytes: Array[Byte]): String = {
    import java.security.MessageDigest
    val digest = MessageDigest.getInstance("MD5")
    digest.reset()
    digest.update(bytes)
    digest.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }

  /**
   * Compute the SHA-1 digest for a `String`.
   *
   * @param text the text to hash
   * @return the SHA-1 digest, encoded as a hex string
   */
  def sha1(text: String): String = sha1(text.getBytes)

  // --

  private val hexChars = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

  /**
   * Converts a byte array into an array of characters that denotes a hexadecimal representation.
   */
  def toHex(array: Array[Byte]): Array[Char] = {
    val result = new Array[Char](array.length * 2)
    for (i <- 0 until array.length) {
      val b = array(i) & 0xff
      result(2 * i) = hexChars(b >> 4)
      result(2 * i + 1) = hexChars(b & 0xf)
    }
    result
  }

  /**
   * Converts a byte array into a `String` that denotes a hexadecimal representation.
   */
  def toHexString(array: Array[Byte]): String = {
    new String(toHex(array))
  }

  /**
   * Transform an hexadecimal String to a byte array.
   */
  def hexStringToByte(hexString: String): Array[Byte] = {
    import org.apache.commons.codec.binary.Hex
    Hex.decodeHex(hexString.toCharArray())
  }

}
object Codecs {
  val encoder = new BASE64Encoder()
  val decoder = new BASE64Decoder()

  def sha1(text: String): String = {
    if (text == null || text.isEmpty()) {
      return ""
    }
    Codec.sha1(text)
  }
  def md5(text: String): String = {
    if (text == null || text.isEmpty()) {
      return ""
    }
    Codec.md5(text.getBytes())
  }
  def encBase64(text: Array[Byte]): String = {
    if (text == null || text.size == 0) {
      return ""
    }
    encoder.encode(text)
  }
  def decBase64(text: String): Array[Byte] = {
    if (text == null || text.isEmpty) {
      return Array[Byte]()
    }
    decoder.decodeBuffer(text)
  }

  def asciiToNative(input: String): String = {
    if (input == null) {
      return input
    }
    var buffer = new StringBuffer(input.length());
    var precedingBackslash = false;
    var i = 0
    val end = input.length()
    while (i < end) {
      var t = input.charAt(i)
      if (precedingBackslash) {
        t = t match {
          case 'f' => '\f'
          case 'n' => '\n'
          case 'r' => '\r'
          case 't' => '\t'
          case 'u' => {
            val hex = input.substring(i + 1, i + 5)
            i = i + 4
            Integer.parseInt(hex, 16).asInstanceOf[Char]
          }
          case _ => t
        }
        precedingBackslash = false;
        buffer.append(t);
        i = i + 1
      } else {
        precedingBackslash = (t == '\\');
        if (!precedingBackslash) {
          buffer.append(t);
        }
        i = i + 1
      }
    }
    return buffer.toString();
  }

}
