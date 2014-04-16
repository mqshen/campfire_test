package org.goldratio.xmpp.auth

/**
 * Created by GoldRatio on 4/9/14.
 */
trait AuthorizationMapping {
  /**
   * Returns true if the principal is explicity authorized to the JID
   *
   * @param principal The autheticated principal requesting authorization.
   * @return The name of the default username to use.
   */
  def map(principal: String): String

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
