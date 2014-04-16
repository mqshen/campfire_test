package org.goldratio.xmpp.processor

import javax.security.auth.callback._
import org.goldratio.xmpp.auth.{UserNotFoundException, AuthToken, AuthorizationManager, AuthFactory}
import javax.security.sasl.{AuthorizeCallback, RealmCallback}
import java.io.IOException
import org.goldratio.xmpp.sasl.VerifyPasswordCallback

/**
 * Created by GoldRatio on 4/9/14.
 */
class XMPPCallbackHandler(authFactory: AuthFactory,
                          authorizationManager: AuthorizationManager) extends CallbackHandler {
  def handle(callbacks: Array[Callback]) {
    var realm: String = null
    var name: String = null
    for (callback <- callbacks) {
      if (callback.isInstanceOf[RealmCallback]) {
        realm = (callback.asInstanceOf[RealmCallback]).getText
        if (realm == null) {
          realm = (callback.asInstanceOf[RealmCallback]).getDefaultText
        }
      }
      else if (callback.isInstanceOf[NameCallback]) {
        name = (callback.asInstanceOf[NameCallback]).getName
        if (name == null) {
          name = (callback.asInstanceOf[NameCallback]).getDefaultName
        }
      }
      else if (callback.isInstanceOf[PasswordCallback]) {
        try {
          (callback.asInstanceOf[PasswordCallback]).setPassword(authFactory.getPassword(name).toCharArray)
        }
        catch {
          case uoe: UnsupportedOperationException => {
            throw new IOException(uoe.toString)
          }
          case e: UserNotFoundException => {
            e.printStackTrace
          }
        }
      }
      else if (callback.isInstanceOf[VerifyPasswordCallback]) {
        val vpcb: VerifyPasswordCallback = callback.asInstanceOf[VerifyPasswordCallback]
        try {
          val at: AuthToken = authFactory.authenticate(name, new String(vpcb.getPassword))
          vpcb.verified = at != null
        }
        catch {
          case e: Exception => {
            vpcb.verified = false
          }
        }
      }
      else if (callback.isInstanceOf[AuthorizeCallback]) {
        val authCallback: AuthorizeCallback = (callback.asInstanceOf[AuthorizeCallback])
        val principal: String = authCallback.getAuthenticationID
        var username: String = authCallback.getAuthorizationID
        if (username != null && username.contains("@")) {
          username = username.substring(0, username.lastIndexOf("@"))
        }
        if (principal == username) {
          username = authorizationManager.map(principal)
        }
        if (authorizationManager.authorize(username, principal)) {
          authCallback.setAuthorized(true)
          authCallback.setAuthorizedID(username)
        }
        else {
          authCallback.setAuthorized(false)
        }
      }
      else {
        throw new UnsupportedCallbackException(callback, "Unrecognized Callback")
      }
    }
  }
}
