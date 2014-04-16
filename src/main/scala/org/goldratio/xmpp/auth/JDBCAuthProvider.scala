package org.goldratio.xmpp.auth

import org.goldratio.xmpp.database.DbConnectionManager

/**
 * Created by GoldRatio on 4/9/14.
 */
class JDBCAuthProvider(connectionManager: DbConnectionManager) extends AuthProvider{
  /**
   * Returns the user's password. This method should throw an UnsupportedOperationException
   * if this operation is not supported by the backend user store.
   *
   * @param username the username of the user.
   * @return the user's password.
   * @throws UserNotFoundException if the given user's password could not be loaded.
   * @throws UnsupportedOperationException if the provider does not
   *                                       support the operation (this is an optional operation).
   */
  override def getPassword(username: String): String = {
    "111"
  }

  /**
   * Returns if the username, token, and digest are valid; otherwise this
   * method throws an UnauthorizedException.<p>
   *
   * If {@link #isDigestSupported()} returns false, this method should
   * throw an UnsupportedOperationException.
   *
   * @param username the username or full JID.
   * @param token the token that was used with plain-text password to
   *              generate the digest.
   * @param digest the digest generated from plain-text password and unique token.
   * @throws UnauthorizedException if the username and password
   *                               do not match any existing user.
   * @throws ConnectionException it there is a problem connecting to user and group sytem
   * @throws InternalUnauthenticatedException if there is a problem authentication Openfire iteself into the user and group system
   */
  override def authenticate(username: String, token: String, digest: String): Unit = {

  }

  /**
   * Returns if the username and password are valid; otherwise this
   * method throws an UnauthorizedException.<p>
   *
   * If {@link #isPlainSupported()} returns false, this method should
   * throw an UnsupportedOperationException.
   *
   * @param username the username or full JID.
   * @param password the password
   * @throws UnauthorizedException if the username and password do
   *                               not match any existing user.
   * @throws ConnectionException it there is a problem connecting to user and group system
   * @throws InternalUnauthenticatedException if there is a problem authentication Openfire itself into the user and group system
   */
  override def authenticate(username: String, password: String): Unit = {

  }

  /**
   * Returns true if this AuthProvider supports digest authentication
   * according to JEP-0078.
   *
   * @return true if digest authentication is supported by this
   *         AuthProvider.
   */
  override def isDigestSupported: Boolean = {
    false
  }

  /**
   * Sets the users's password. This method should throw an UnsupportedOperationException
   * if this operation is not supported by the backend user store.
   *
   * @param username the username of the user.
   * @param password the new plaintext password for the user.
   * @throws UnsupportedOperationException if the provider does not
   *                                       support the operation (this is an optional operation).
   */
  override def setPassword(username: String, password: String): Unit = {

  }

  /**
   * Returns true if this UserProvider is able to retrieve user passwords from
   * the backend user store. If this operation is not supported then {@link #getPassword(String)}
   * will throw an {@link UnsupportedOperationException} if invoked.
   *
   * @return true if this UserProvider is able to retrieve user passwords from the
   *         backend user store.
   */
  override def supportsPasswordRetrieval: Boolean = {
    false
  }
}
