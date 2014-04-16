package org.goldratio.xmpp.auth

/**
 * Created by GoldRatio on 4/9/14.
 */
trait AuthorizationPolicy {
  /**
   * Returns true if the principal is explicitly authorized to the JID
   *
   * @param username  The username requested.
   * @param principal The principal requesting the username.
   * @return true is the user is authorized to be principal
   */
  def authorize(username: String, principal: String): Boolean

  /**
   * Returns the short name of the Policy
   *
   * @return The short name of the Policy
   */
  def name: String

  /**
   * Returns a description of the Policy
   *
   * @return The description of the Policy.
   */
  def description: String

}
