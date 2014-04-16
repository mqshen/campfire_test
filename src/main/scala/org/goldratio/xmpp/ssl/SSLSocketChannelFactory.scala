package org.goldratio.xmpp.ssl

/**
 * Created by GoldRatio on 4/9/14.
 */
import org.goldratio.util.ssl.CertificateEntry
import org.goldratio.util.ssl.CertificateUtil

import javax.net.ssl._
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate

/**
 * Created by GoldRatio on 3/26/14.
 */
object SSLSocketChannelFactory {
  val emptyPass = new Array[Char](0)
  val alias = "mytask.com"

  val TLS_WORKAROUND_CIPHERS = Array( "SSL_RSA_WITH_RC4_128_MD5",
    "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
    "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA",
    "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
    "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
    "TLS_EMPTY_RENEGOTIATION_INFO_SCSV" )

  val HARDENED_MODE_FORBIDDEN_SIPHERS = ( "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
    "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
    "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", "TLS_ECDHE_RSA_WITH_RC4_128_SHA", "SSL_RSA_WITH_RC4_128_SHA",
    "TLS_ECDH_ECDSA_WITH_RC4_128_SHA", "TLS_ECDH_RSA_WITH_RC4_128_SHA", "SSL_RSA_WITH_RC4_128_MD5",
    "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5",
    "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5" )

  def build(): SSLEngine = {
    try {

      val keyPair = CertificateUtil.createKeyPair(1024, "secret")
      val cert = CertificateUtil.createSelfSignedCertificate("admin@mytask.com", alias, "XMPP Service", "mytask.com", null, null, null, keyPair)

      val certs = new Array[Certificate](1)
      certs(0) = cert
      val entry = new CertificateEntry(certs, keyPair.getPrivate())

      val keys = KeyStore.getInstance("JKS")

      keys.load(null, emptyPass)
      keys.setKeyEntry(alias, entry.privateKey, emptyPass, CertificateUtil.sort(entry.chain))


      val kmf = KeyManagerFactory.getInstance("SunX509")

      kmf.init(keys, emptyPass)

      val secureRandom = new SecureRandom()

      val tms = new Array[TrustManager](1)
      tms(0) = new FakeTrustManager()

      val sslContext = SSLContext.getInstance("SSL")
      sslContext.init(kmf.getKeyManagers(), tms, secureRandom)


      val tlsEngine = sslContext.createSSLEngine()
      tlsEngine.setUseClientMode(false)

      tlsEngine.setEnabledCipherSuites(TLS_WORKAROUND_CIPHERS)

      tlsEngine
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        null
    }
  }

  class FakeTrustManager(issuers: Array[X509Certificate] = null) extends X509TrustManager {

    override def checkClientTrusted(x509CertificateArray: Array[X509Certificate], string: String) {
    }

    override def checkServerTrusted(x509CertificateArray: Array[X509Certificate], string: String) {
    }

    override def getAcceptedIssuers(): Array[X509Certificate] = {
      issuers
    }
  }
}

