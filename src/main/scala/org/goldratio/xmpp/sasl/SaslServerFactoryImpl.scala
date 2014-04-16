package org.goldratio.xmpp.sasl

import javax.security.sasl.{SaslException, SaslServer, SaslServerFactory}
import java.util
import javax.security.auth.callback.CallbackHandler
import com.sun.security.sasl.util.PolicyUtils

/**
 * Created by GoldRatio on 4/9/14.
 */
object SaslServerFactoryImpl {
  val myMechs = Array("PLAIN", "CLEARSPACE")
  val mechPolicies = Array(PolicyUtils.NOANONYMOUS, PolicyUtils.NOANONYMOUS)
  val PLAIN: Int = 0
  val CLEARSPACE: Int = 1
}

class SaslServerFactoryImpl extends SaslServerFactory{
  import SaslServerFactoryImpl._

  override def getMechanismNames(props: util.Map[String, _]): Array[String] = {
    PolicyUtils.filterMechs(myMechs, mechPolicies, props);
  }

  override def createSaslServer(mechanism: String, protocol: String, serverName: String, props: util.Map[String, _], cbh: CallbackHandler): SaslServer = {
    if ((mechanism == myMechs(PLAIN)) && PolicyUtils.checkPolicy(mechPolicies(PLAIN), props)) {
      if (cbh == null) {
        throw new SaslException("CallbackHandler with support for Password, Name, and AuthorizeCallback required")
      }
      new SaslServerPlainImpl(cbh)
    }
    else if ((mechanism == myMechs(CLEARSPACE)) && PolicyUtils.checkPolicy(mechPolicies(CLEARSPACE), props)) {
      throw new SaslException("CallbackHandler with support for AuthorizeCallback required")
    }
    else
      null
  }
}
