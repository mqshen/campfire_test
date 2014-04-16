package org.goldratio.xmpp.user

/**
 * Created by GoldRatio on 4/9/14.
 */
trait UserProvider {
  /**
   * Creates a new user. This method should throw an
   * UnsupportedOperationException if this operation is not
   * supporte by the backend user store.
   *
   * @param username the username.
   * @param password the plain-text password.
   * @param name the user's name, which can be <tt>null</tt>, unless isNameRequired is set to true.
   * @param email the user's email address, which can be <tt>null</tt>, unless isEmailRequired is set to true.
   * @return a new User.
   * @throws UserAlreadyExistsException if the username is already in use.
   */
  def createUser(username: String, password: String, name: String, email: String): User

  /**
   * Delets a user. This method should throw an
   * UnsupportedOperationException if this operation is not
   * supported by the backend user store.
   *
   * @param username the username to delete.
   */
  def deleteUser(username: String)

  /**
   * Returns the number of users in the system.
   *
   * @return the total number of users.
   */
  def getUserCount: Int

  /**
   * Loads the specified user by username.
   *
   * @param username the username
   * @return the User.
   * @throws UserNotFoundException if the User could not be loaded.
   */
  def loadUser(username: String): User

}
