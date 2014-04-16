package org.goldratio.xmpp.sasl

import javax.security.auth.callback.Callback

/**
 * Created by GoldRatio on 4/9/14.
 */
class VerifyPasswordCallback(var password: Array[Char], var verified: Boolean = false) extends Callback with Serializable {

  def getPassword(): Array[Char] = {
    if(password != null)
      password.clone()
    else
      null
  }

  /**
   * Clear the retrieved password.
   */
  def clearPassword {
    if (password != null) {
      (0 until(password.length)).foreach{i =>
        password(i) = ' '
      }
      password = null
    }
  }

}
