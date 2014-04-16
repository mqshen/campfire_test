package org.goldratio.xmpp.auth

import scala.collection.mutable.ArrayBuffer
import org.goldratio.xmpp.user.UserManager


/**
 * Created by GoldRatio on 4/9/14.
 */
class AuthorizationManager(userManager: UserManager) {
  val authorizationPolicies: ArrayBuffer[AuthorizationPolicy] = new ArrayBuffer[AuthorizationPolicy]
  val authorizationMapping: ArrayBuffer[AuthorizationMapping] = new ArrayBuffer[AuthorizationMapping]

  def addAuthorizationPolicy(authorizationPolicy: AuthorizationPolicy) {
    authorizationPolicies.append(authorizationPolicy)
  }

  def addAuthorizationMapping(authorizationMap: AuthorizationMapping) {
    authorizationMapping.append(authorizationMap)
  }

  /**
   * Returns the currently-installed AuthorizationProvider. Warning: You
   * should not be calling the AuthorizationProvider directly to perform
   * authorizations, it will not take into account the policy selected in
   * the <tt>openfire.xml</tt>.  Use @see{authorize} in this class, instead.
   *
   * @return the current AuthorizationProvider.
   */
  def getAuthorizationPolicies: List[AuthorizationPolicy] = {
    authorizationPolicies.toList
  }

  /**
   * Authorize the authenticated used to the requested username.  This uses the
   * selected the selected AuthenticationProviders.
   *
   * @param username The requested username.
   * @param principal The authenticated principal.
   * @return true if the user is authorized.
   */
  def authorize(username: String, principal: String): Boolean = {
    for (ap <- authorizationPolicies) {
      if (ap.authorize(username, principal)) {
        try {
          userManager.provider.loadUser(username)
        }
        catch {
          case nfe: UserNotFoundException => {
            return false
          }
        }
        return true
      }
    }
    return false
  }

  /**
   * Map the authenticated principal to the default username.  If the authenticated
   * principal did not supply a username, determine the default to use.
   *
   * @param principal The authentiated principal to determine the default username.
   * @return The default username for the authentiated principal.
   */
  def map(principal: String): String = {
    import scala.collection.JavaConversions._
    for (am <- authorizationMapping) {
      val username: String = am.map(principal)
      if (!(username == principal)) {
        return username
      }
    }
    principal
  }
}
