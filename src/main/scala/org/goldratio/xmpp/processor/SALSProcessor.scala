package org.goldratio.xmpp.processor

import java.security.Security
import org.goldratio.xmpp.sasl.SaslProvider
import org.goldratio.xmpp.protocol.{JID, SaslAuth}
import javax.security.sasl.{SaslException, Sasl, SaslServer}
import scala.collection.mutable.{Set, HashSet}
import org.apache.commons.codec.binary.Base64
import org.goldratio.xmpp.database.DbConnectionManager
import org.goldratio.xmpp.user.{UserManager, JDBCUserProvider}
import org.goldratio.xmpp.auth._
import scala.Some
import org.goldratio.core.Constance

/**
 * Created by GoldRatio on 4/9/14.
 */
class SASLProcessor(callbackHandler: XMPPCallbackHandler) {


  Security.addProvider(new SaslProvider)

  val mechanisms: Set[String] = new HashSet[String]()

  mechanisms.add("PLAIN")
  mechanisms.add("DIGEST-MD5")
  mechanisms.add("CRAM-MD5")

  def process(auth: SaslAuth): Option[JID] = {

    val mechanism: String = auth.mechanism.toString

    if (mechanisms.contains(mechanism)) {
      try {
        val ss: SaslServer = Sasl.createSaslServer(mechanism, "xmpp", Constance.domain, null, callbackHandler)
        if (ss != null) {
          val encode: String = auth.value
          if (encode.length > 0) {
            val token: Array[Byte] = Base64.decodeBase64(encode)
            val challenge: Array[Byte] = ss.evaluateResponse(token)
            if (ss.isComplete) {
              val userName: String = ss.getAuthorizationID
              val jid: JID = JID(userName, Constance.domain)
              Some(jid)
            }
            else {
              None
            }
          }
          else {
            None
          }
        }
        else {
          None
        }
      }
      catch {
        case e: SaslException => {
          e.printStackTrace
        }
        None
      }
    }
    else
      None
  }

}
