package org.goldratio.xmpp.auth

/**
 * Created by GoldRatio on 4/9/14.
 */
class DefaultAuthorizationPolicy extends AuthorizationPolicy {
  /**
   * Returns true if the principal is explicitly authorized to the JID
   *
   * @param username  The username requested.
   * @param authenID The authenticated ID (principal) requesting the username.
   * @return true if the authenticated ID is authorized to the requested user.
   */
  def authorize(username: String, authenID: String): Boolean = {
    var userUser: String = username
    var authenUser: String = authenID
    if (username.contains("@")) {
      userUser = username.substring(0, username.lastIndexOf("@"))
    }
    if (authenID.contains("@")) {
      authenUser = authenID.substring(0, (authenID.lastIndexOf("@")))
    }
    if (userUser == authenUser) {
      true
    }
    else
    false
  }

  /**
   * Returns the short name of the Policy
   *
   * @return The short name of the Policy
   */
  def name: String = {
    "Default Policy"
  }

  /**
   * Returns a description of the Policy
   *
   * @return The description of the Policy.
   */
  def description: String = {
    "Different clients perform authentication differently, so this policy " + "will authorize any principal to a requested user that match specific " + "conditions that are considered secure defaults for most installations."
  }
}

