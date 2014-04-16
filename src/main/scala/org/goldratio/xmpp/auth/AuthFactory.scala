package org.goldratio.xmpp.auth

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64

/**
 * Created by GoldRatio on 4/9/14.
 */
class AuthFactory(authProvider: AuthProvider) {
  val digest: MessageDigest = MessageDigest.getInstance("SHA")

  var cipher:Blowfish = null
  private object DIGEST_LOCK

  def isDigestSupported: Boolean = {
    return authProvider.isDigestSupported
  }

  /**
   * Returns the user's password. This method will throw an UnsupportedOperationException
   * if this operation is not supported by the backend user store.
   *
   * @param username the username of the user.
   * @return the user's password.
   * @throws UserNotFoundException if the given user could not be found.
   * @throws UnsupportedOperationException if the provider does not
   *                                       support the operation (this is an optional operation).
   */
  def getPassword(username: String): String = {
    return authProvider.getPassword(username.toLowerCase)
  }

  /**
   * Authenticates a user with a username and plain text password and returns and
   * AuthToken. If the username and password do not match the record of
   * any user in the system, this method throws an UnauthorizedException.
   *
   * @param username the username.
   * @param password the password.
   * @return an AuthToken token if the username and password are correct.
   * @throws UnauthorizedException if the username and password do not match any existing user
   *                               or the account is locked out.
   */
  def authenticate(username: String, password: String): AuthToken = {
    authProvider.authenticate(username, password)
    return new AuthToken(username)
  }

  /**
   * Authenticates a user with a username, token, and digest and returns an AuthToken.
   * The digest should be generated using the {@link #createDigest(String, String)} method.
   * If the username and digest do not match the record of any user in the system, the
   * method throws an UnauthorizedException.
   *
   * @param username the username.
   * @param token the token that was used with plain-text password to generate the digest.
   * @param digest the digest generated from plain-text password and unique token.
   * @return an AuthToken token if the username and digest are correct for the user's
   *         password and given token.
   * @throws UnauthorizedException if the username and password do not match any
   *                               existing user or the account is locked out.
   */
  def authenticate(username: String, token: String, digest: String): AuthToken = {
    authProvider.authenticate(username, token, digest)
    return new AuthToken(username)
  }

  /**
   * Returns a digest given a token and password, according to JEP-0078.
   *
   * @param token the token used in the digest.
   * @param password the plain-text password to be digested.
   * @return the digested result as a hex string.
   */
  def createDigest(token: String, password: String): String = {
    DIGEST_LOCK synchronized {
      digest.update(token.getBytes)
      return new String(Base64.encodeBase64(digest.digest(password.getBytes)))
    }
  }

  /**
   * Returns an encrypted version of the plain-text password. Encryption is performed
   * using the Blowfish algorithm. The encryption key is stored as the Jive property
   * "passwordKey". If the key is not present, it will be automatically generated.
   *
   * @param password the plain-text password.
   * @return the encrypted password.
   * @throws UnsupportedOperationException if encryption/decryption is not possible;
   *                                       for example, during setup mode.
   */
  def encryptPassword(password: String): String = {
    if (password == null) {
      return null
    }
    val cipher: Blowfish = getCipher
    if (cipher == null) {
      throw new UnsupportedOperationException
    }
    return cipher.encryptString(password)
  }

  /**
   * Returns a decrypted version of the encrypted password. Encryption is performed
   * using the Blowfish algorithm. The encryption key is stored as the Jive property
   * "passwordKey". If the key is not present, it will be automatically generated.
   *
   * @param encryptedPassword the encrypted password.
   * @return the encrypted password.
   * @throws UnsupportedOperationException if encryption/decryption is not possible;
   *                                       for example, during setup mode.
   */
  def decryptPassword(encryptedPassword: String): String = {
    if (encryptedPassword == null) {
      return null
    }
    val cipher: Blowfish = getCipher
    if (cipher == null) {
      throw new UnsupportedOperationException
    }
    return cipher.decryptString(encryptedPassword)
  }

  /**
   * Returns a Blowfish cipher that can be used for encrypting and decrypting passwords.
   * The encryption key is stored as the Jive property "passwordKey". If it's not present,
   * it will be automatically generated.
   *
   * @return the Blowfish cipher, or <tt>null</tt> if Openfire is not able to create a Cipher;
   *         for example, during setup mode.
   */
  private def getCipher: Blowfish = {
    if (cipher != null) {
      return cipher
    }
    val keyString: String = "password"
    try {
      cipher = new Blowfish(keyString)
    }
    catch {
      case e: Exception => {
      }
    }
    return cipher
  }

}
