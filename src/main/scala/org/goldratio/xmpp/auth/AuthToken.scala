package org.goldratio.xmpp.auth

/**
 * Created by GoldRatio on 4/9/14.
 */
object AuthToken {
  def apply(jid: String): AuthToken = apply(jid, false)

  def apply(jid: String, anonymous: Boolean) = {
    val index: Int = jid.indexOf("@")
    if (index > -1) {
      new AuthToken(jid.substring(0, index), jid.substring(index + 1), anonymous)
    }
    else {
      new AuthToken(jid, anonymous = anonymous)
    }
  }
}
class AuthToken(val username: String, val domain: String = "mytask.com", val anonymous: Boolean = false) {

}
