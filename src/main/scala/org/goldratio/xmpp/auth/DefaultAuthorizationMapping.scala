package org.goldratio.xmpp.auth

/**
 * Created by GoldRatio on 4/9/14.
 */
class DefaultAuthorizationMapping extends AuthorizationMapping {

  /**
   * Returns true if the principal is explicity authorized to the JID
   *
   * @param principal The autheticated principal requesting authorization.
   * @return The name of the default username to use.
   */
  def map(principal: String): String = {
    if (principal.contains("@")) {
      val realm: String = principal.substring(principal.lastIndexOf('@') + 1)
      val username: String = principal.substring(0, principal.lastIndexOf('@'))
      if (realm.length > 0) {
        if (realm == "mytask.com") {
          return username
        }
        else
          principal
      }
      else
        principal
    }
    else
      principal
  }

  /**
   * Returns the short name of the Policy
   *
   * @return The short name of the Policy
   */
  def name: String = {
    "Default Mapping"
  }

  /**
   * Returns a description of the Policy
   *
   * @return The description of the Policy.
   */
  def description: String = {
    "Simply remove's the realm of the requesting principal if and only if " + "the realm matches the server's realm or the server's xmpp domain name. " + "Otherwise the principal is used as the username."
  }
}

