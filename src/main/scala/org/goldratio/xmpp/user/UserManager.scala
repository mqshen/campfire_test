package org.goldratio.xmpp.user


/**
 * Created by GoldRatio on 4/9/14.
 */
class UserManager(val provider: UserProvider) {
  def createUser(username: String, password: String, name: String, email: String): User = {
    val user: User = provider.createUser(username, password, name, email)
    user
  }

  def isRegisteredUser(username: String): Boolean = {
    false
  }


}
