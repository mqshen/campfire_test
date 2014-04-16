package org.goldratio.xmpp.sasl

import javax.security.sasl.{Sasl, SaslException, AuthorizeCallback, SaslServer}
import javax.security.auth.callback.{UnsupportedCallbackException, Callback, NameCallback, CallbackHandler}
import java.util.StringTokenizer
import java.io.{IOException, UnsupportedEncodingException}

/**
 * Created by GoldRatio on 4/9/14.
 */
class SaslServerPlainImpl(cbh: CallbackHandler,
                          var counter: Int = 0,
                          var completed: Boolean= false
                          ) extends SaslServer{
  var principal: String = null
  var username: String = null//requested authorization identity
  var password: String= null
  var aborted: Boolean= false


  def getMechanismName(): String = {
    "PLAIN"
  }

  def evaluateResponse(response: Array[Byte]): Array[Byte] ={
    if (completed) {
      throw new IllegalStateException("PLAIN authentication already completed")
    }
    if (aborted) {
      throw new IllegalStateException("PLAIN authentication previously aborted due to error")
    }
    try {
      if(response.length != 0) {
        val data = new String(response, "UTF8")
        val tokens = new StringTokenizer(data, "\0")
        if (tokens.countTokens() > 2) {
          username = tokens.nextToken()
          principal = tokens.nextToken()
        } else {
          username = tokens.nextToken()
          principal = username
        }
        password = tokens.nextToken()
        val ncb = new NameCallback("PLAIN authentication ID: ",principal)
        val vpcb = new VerifyPasswordCallback(password.toCharArray())
        cbh.handle(Array[Callback](ncb,vpcb))

        if (vpcb.verified) {
          vpcb.clearPassword
          val acb = new AuthorizeCallback(principal,username)
          cbh.handle(Array[Callback](acb))
          if(acb.isAuthorized()) {
            username = acb.getAuthorizedID()
            completed = true
          } else {
            completed = true
            username = null
            throw new SaslException("PLAIN: user not authorized: "+principal)
          }
        } else {
          throw new SaslException("PLAIN: user not authorized: "+principal)
        }
      }
      else {
        if( counter > 1 ) {
          throw new SaslException("PLAIN expects a response")
        }
        counter += 1
        null
      }
    }
    catch  {
      case e: UnsupportedEncodingException =>
        aborted = true
        throw new SaslException("UTF8 not available on platform", e)
      case e: UnsupportedCallbackException =>
        aborted = true
        throw new SaslException("PLAIN authentication failed for: "+username, e)
      case e: IOException =>
        aborted = true
        throw new SaslException("PLAIN authentication failed for: "+username, e)
    }
    null
  }

  /**
   * Determines whether the authentication exchange has completed.
   * This method is typically called after each invocation of
   * <tt>evaluateResponse()</tt> to determine whether the
   * authentication has completed successfully or should be continued.
   * @return true if the authentication exchange has completed false otherwise.
   */

  /**
   * Reports the authorization ID in effect for the client of this
   * session.
   * This method can only be called if isComplete() returns true.
   * @return The authorization ID of the client.
   */
  def getAuthorizationID(): String = {
    if(completed) {
      username
    } else {
      throw new IllegalStateException("PLAIN authentication not completed")
    }
  }



  /**
   * Retrieves the negotiated property.
   * This method can be called only after the authentication exchange has
   * completed (i.e., when <tt>isComplete()</tt> returns true) otherwise, an
   * <tt>IllegalStateException</tt> is thrown.
   *
   * @param propName the property
   * @return The value of the negotiated property. If null, the property was
   * not negotiated or is not applicable to this mechanism.
   */

  def getNegotiatedProperty(propName: String): AnyRef = {
    if (completed) {
      if (propName.equals(Sasl.QOP)) {
        "auth"
      } else {
        null
      }
    } else {
      throw new IllegalStateException("PLAIN authentication not completed")
    }
  }

  /**
   * Disposes of any system resources or security-sensitive information
   * the SaslServer might be using. Invoking this method invalidates
   * the SaslServer instance. This method is idempotent.
   * @throws SaslException If a problem was encountered while disposing
   * the resources.
   */
  def dispose() {
    password = null
    username = null
    principal = null
    completed = false
  }

  override def wrap(outgoing: Array[Byte], offset: Int, len: Int): Array[Byte] = {
    if(completed) {
      throw new IllegalStateException("PLAIN does not support integrity or privacy")
    } else {
      throw new IllegalStateException("PLAIN authentication not completed")
    }
  }

  override def unwrap(incoming: Array[Byte], offset: Int, len: Int): Array[Byte] = {
    if(completed) {
      throw new IllegalStateException("PLAIN does not support integrity or privacy")
    } else {
      throw new IllegalStateException("PLAIN authentication not completed")
    }
  }

  override def isComplete: Boolean = {
    completed
  }
}
