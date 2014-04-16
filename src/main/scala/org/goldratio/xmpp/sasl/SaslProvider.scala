package org.goldratio.xmpp.sasl

import java.security.Provider

/**
 * Created by GoldRatio on 4/9/14.
 */
class SaslProvider extends Provider("goldratio", 1.0, "JiveSoftware SASL provider v1.0, implementing server mechanisms for: PLAIN, CLEARSPACE"){

  put("SaslServerFactory.PLAIN", "org.goldratio.xmpp.sasl.SaslServerFactoryImpl")

}
