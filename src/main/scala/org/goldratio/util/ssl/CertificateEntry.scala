package org.goldratio.util.ssl

/**
 * Created by GoldRatio on 4/9/14.
 */

import java.security.PrivateKey
import java.security.cert.Certificate

/**
 * Created by GoldRatio on 3/26/14.
 */

class CertificateEntry( var chain:Array[Certificate] = null,
  var privateKey: PrivateKey = null ) {


  override def toString(): String = {
    val sb = new StringBuilder(4096)
    chain.toList.foreach { cert =>
      sb.append(cert.toString)
    }
    "Private key: " + privateKey.toString() + '\n' + sb
  }
}



