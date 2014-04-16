package org.goldratio.util.ssl

/**
 * Created by GoldRatio on 4/9/14.
 */
import scala.collection.JavaConversions._
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.Reader
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertPathValidator
import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.PKIXBuilderParameters
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.ArrayList
import java.util.Date
import java.util.List

import javax.crypto.Cipher

import org.goldratio.util.Algorithms
import sun.security.x509.AlgorithmId
import sun.security.x509.CertificateAlgorithmId
import sun.security.x509.CertificateIssuerName
import sun.security.x509.CertificateSerialNumber
import sun.security.x509.CertificateSubjectName
import sun.security.x509.CertificateValidity
import sun.security.x509.CertificateVersion
import sun.security.x509.CertificateX509Key
import sun.security.x509.X500Name
import sun.security.x509.X509CertImpl
import sun.security.x509.X509CertInfo
import sun.misc.{BASE64Decoder, BASE64Encoder}
import org.goldratio.util.ssl.CertCheckResult.CertCheckResult
import java.util

/**
 * Created: Sep 22, 2010 3:09:01 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
object CertificateUtil {
  val encoder = new BASE64Encoder()
  val decoder = new BASE64Decoder()

  val BEGIN_CERT = "-----BEGIN CERTIFICATE-----"
  val BEGIN_KEY = "-----BEGIN PRIVATE KEY-----"
  val BEGIN_RSA_KEY = "-----BEGIN RSA PRIVATE KEY-----"
  val ENCRIPT_TEST = "--encript-test"
  val ENCRIPT_TEST_SHORT = "-et"
  val END_CERT = "-----END CERTIFICATE-----"
  val END_KEY = "-----END PRIVATE KEY-----"
  val END_RSA_KEY = "-----END RSA PRIVATE KEY-----"
  val ID_ON_XMPPADDR = Array[Byte]( 0x06, 0x08, 0x2B, 0x06, 0x01, 0x05, 0x05, 0x07, 0x08, 0x05 )
  val KEY_PAIR = "--key-pair"
  val KEY_PAIR_SHORT = "-kp"
  val LOAD_CERT = "--load-cert"
  val LOAD_CERT_SHORT = "-lc"
  val LOAD_DER_PRIVATE_KEY = "--load-der-priv-key"
  val LOAD_DER_PRIVATE_KEY_SHORT = "-ldpk"
  val PRINT_PROVIDERS = "--print-providers"
  val PRINT_PROVIDERS_SHORT = "-pp"
  val PRINT_SERVICES = "--print-services"
  val PRINT_SERVICES_SHORT = "-ps"
  val SELF_SIGNED_CERT = "--self-signed-cert"
  val SELF_SIGNED_CERT_SHORT = "-ssc"
  val STORE_CERT = "--store-cert"

  // ~--- methods
  // --------------------------------------------------------------

  val STORE_CERT_SHORT = "-sc"

  def appendName(sb: StringBuilder , prefix: String , value: String ) {
    if (value != null) {
      if (sb.length > 0) {
        sb.append(", ")
      }
      sb.append(prefix).append('=').append(value)
    }
  }

  def calculateLength(buffer: Array[Byte], start: Int): Int = {
    var offset = start + 1
    var b = (buffer(offset) & 0xff)
    if (b < 0x80)
      b
    else {
      var result = 0
      offset += 1
      val len = b - 0x80
      (0 until len).foreach{i =>
        b = buffer(i + offset) & 0xff
        result = (result << 8) + b
      }
      result
    }
  }

  def calculateOffset(buffer: Array[Byte], offset: Int):Int = {
    val b = (buffer(offset + 1) & 0xff)
    if (b < 0x80)
      (offset + 2)
    else {
      val len = b - 0x80
      (offset + len + 2)
    }
  }

  def createKeyPair(size: Int, password: String): KeyPair = {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")

    keyPairGenerator.initialize(size)

    keyPairGenerator.genKeyPair()
  }

  def createSelfSignedCertificate(email: String, domain: String, organizationUnit: String,
    organization: String, city: String, state: String , country: String , keyPair: KeyPair ): X509Certificate = {
    val certInfo = new X509CertInfo()
    val certVersion = new CertificateVersion()

    certInfo.set(X509CertInfo.VERSION, certVersion)

    val firstDate = new Date()
    val lastDate = new Date(firstDate.getTime() + 365 * 24 * 60 * 60 * 1000L)
    val interval = new CertificateValidity(firstDate, lastDate)

    certInfo.set(X509CertInfo.VALIDITY, interval)
    certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber((firstDate.getTime() / 1000).asInstanceOf[Int]))

    val subject = new StringBuilder(1024)

    appendName(subject, "CN", domain)
    appendName(subject, "CN", "*." + domain)
    appendName(subject, "EMAILADDRESS", email)
    appendName(subject, "OU", organizationUnit)
    appendName(subject, "O", organization)
    appendName(subject, "L", city)
    appendName(subject, "ST", state)
    appendName(subject, "C", country)

    val issuerName = new X500Name(subject.toString())
    val certIssuer = new CertificateIssuerName(issuerName)
    val certSubject = new CertificateSubjectName(issuerName)

    certInfo.set(X509CertInfo.ISSUER, certIssuer)
    certInfo.set(X509CertInfo.SUBJECT, certSubject)

    // certInfo.set(X509CertInfo.ISSUER + "." +
    // CertificateSubjectName.DN_NAME, issuerName)
    val algorithm = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)
    val certAlgorithm = new CertificateAlgorithmId(algorithm)

    certInfo.set(X509CertInfo.ALGORITHM_ID, certAlgorithm)

    val certPublicKey = new CertificateX509Key(keyPair.getPublic())

    certInfo.set(X509CertInfo.KEY, certPublicKey)

    // certInfo.set(X509CertInfo.ALGORITHM_ID + "." +
    // CertificateAlgorithmId.ALGORITHM, algorithm)
    val newCert = new X509CertImpl(certInfo)

    newCert.sign(keyPair.getPrivate(), "SHA1WithRSA")

    newCert
  }

  def encriptTest() {

    // KeyPair test:
    // 1. Generating key pair:
    System.out.print("Generating key pair...")
    System.out.flush()

    val keyPair = createKeyPair(1024, "secret")

    System.out.println(" done.")

    // Encryption/decription test
    val inputText = "Encription test...".getBytes()
    val cipher = Cipher.getInstance("RSA")

    System.out.println("Encripting text: " + new String(inputText))
    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic())

    val cipherText = cipher.doFinal(inputText)

    System.out.println("Encripted text: " + Algorithms.bytesToHex(cipherText))
    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate())

    val plainText = cipher.doFinal(cipherText)

    System.out.println("Decripted text: " + new String(plainText))
  }

  def exportToPemFormat(entry: CertificateEntry): String = {
    val sb = new StringBuilder(4096)

    if ((entry.chain != null) && (entry.chain.length > 0)) {
      val bytes = entry.chain(0).getEncoded()
      val b64 = new String(encoder.encode(bytes))

      sb.append(BEGIN_CERT).append('\n').append(b64).append('\n').append(END_CERT).append('\n')
    }

    if (entry.privateKey != null) {
      val bytes = entry.privateKey.getEncoded()
      val b64 = new String(encoder.encode(bytes))

      sb.append(BEGIN_KEY).append('\n').append(b64).append('\n').append(END_KEY).append('\n')
    }

    if ((entry.chain != null) && (entry.chain.length > 1)) {
      entry.chain.foreach { cert =>
        val bytes = cert.getEncoded
        val b64 = new String(encoder.encode(bytes))
        sb.append(BEGIN_CERT).append('\n').append(b64).append('\n').append(END_CERT).append('\n')
      }
    }
    sb.toString()
  }

  // ~--- get methods
  // ----------------------------------------------------------

  def extractValue(buffer: Array[Byte], id: Array[Byte]):String = {
    try {
      if (buffer(0) != 0x30)
        null
      else {
        val len = calculateLength(buffer, 0)
        val offset = calculateOffset(buffer, 0)

        (0 until id.length).foreach{ i =>
          val j = offset + i
          if (j >= len)
            null
          if (id(i) != buffer(j))
            null

        }
        val valStart = offset + id.length

        var pos = calculateOffset(buffer, valStart)

        while (pos < buffer.length) {
          val d = buffer(pos)
          val cmp = calculateOffset(buffer, pos)
          val l = calculateLength(buffer, pos)
          if (d == 0x0c || d == 0x16) {
            new String(buffer, cmp, l)
          }
          pos = cmp
        }
        null
      }
    }
    catch  {
      case e: ArrayIndexOutOfBoundsException =>
        null
    }
  }

  def extractXmppAddrs(x509Certificate: X509Certificate ): List[String] = {
    val result = new ArrayList[String]()
    try {
      val altNames = x509Certificate.getSubjectAlternativeNames()
      if (altNames == null)
        return result
      altNames.toList.foreach { item =>
        val itemType = item.get(0).asInstanceOf[Int]
        if(itemType == 0) {
          val buffer = item.get(1).asInstanceOf[Array[Byte]]
          val jid = extractValue(buffer, ID_ON_XMPPADDR)
          if (jid != null) {
            result.add(jid)
          }
        }
      }
      result
    }
    catch {
      case e: Exception =>
        result
    }
  }


  def getCertCName(cert: X509Certificate ): String = {
    val princ = cert.getSubjectX500Principal()
    val name = princ.getName()
    val all = name.split(",")
    all.foreach { n =>
      val ns = n.trim().split("=")
      if (ns(0).equals("CN")) {
        ns(1)
      }
    }
    return null
  }



  def isExpired(cert: X509Certificate ): Boolean = {
    try {
      cert.checkValidity()

      return false
    } catch {
      case e: Exception =>
        true
    }
  }

  def isSelfSigned(cert: X509Certificate ): Boolean = {
    cert.getIssuerDN().equals(cert.getSubjectDN())
  }

  def keyPairTest() {

    // KeyPair test:
    // 1. Generating key pair:
    System.out.print("Generating key pair...")
    System.out.flush()

    val keyPair = createKeyPair(1024, "secret")

    System.out.println(" done, private key: " + keyPair.getPrivate() + ", public key: " + keyPair.getPublic())
  }


  def loadCertificate(file: File ): CertificateEntry  = {
    parseCertificate(new FileReader(file))
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   *
   * @throws CertificateException
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   */
  def loadCertificate(file: String ): CertificateEntry = {
    loadCertificate(new File(file))
  }


  def loadPrivateKeyFromDER(file: File ): PrivateKey = {
    val dis = new DataInputStream(new FileInputStream(file))
    val privKeyBytes = new Array[Byte](file.length().asInstanceOf[Int])

    dis.read(privKeyBytes)
    dis.close()

    val keyFactory = KeyFactory.getInstance("RSA")
    val privSpec = new PKCS8EncodedKeySpec(privKeyBytes)
    keyFactory.generatePrivate(privSpec)
  }

  /**
   * Method description
   *
   *
   *
   * @throws Exception
   */
//  public static void main(String[] args) throws Exception {
//    if ((args != null) && (args.length > 0)) {
//      if (args[0].equals(PRINT_PROVIDERS) || args[0].equals(PRINT_PROVIDERS_SHORT)) {
//        printProviders(false)
//      }
//
//      if (args[0].equals(PRINT_SERVICES) || args[0].equals(PRINT_SERVICES_SHORT)) {
//        printProviders(true)
//      }
//
//      if (args[0].equals(KEY_PAIR) || args[0].equals(KEY_PAIR_SHORT)) {
//        keyPairTest()
//      }

//      if (args[0].equals(ENCRIPT_TEST) || args[0].equals(ENCRIPT_TEST_SHORT)) {
//        encriptTest()
//      }
//
//      if (args[0].equals(SELF_SIGNED_CERT) || args[0].equals(SELF_SIGNED_CERT_SHORT)) {
//        selfSignedCertTest()
//      }
//
//      if (args[0].equals(LOAD_CERT) || args[0].equals(LOAD_CERT_SHORT)) {
//        String file = args[1]
//        CertificateEntry ce = loadCertificate(file)
//
//        System.out.println(ce.toString())
//      }
//
//      if (args[0].equals(STORE_CERT) || args[0].equals(STORE_CERT_SHORT)) {
//        String file = args[1]
//
//        // Certificate
//        String email = "artur.hefczyc@tigase.org"
//        String domain = "tigase.org"
//        String ou = "XMPP Service"
//        String o = "Tigase.org"
//        String l = "Cambourne"
//        String st = "Cambridgeshire"
//        String c = "UK"
//        KeyPair keyPair = createKeyPair(1024, "secret")
//        X509Certificate cert = createSelfSignedCertificate(email, domain, ou, o, l, st, c, keyPair)
//        CertificateEntry entry = new CertificateEntry()
//
//        entry.setPrivateKey(keyPair.getPrivate())
//        entry.setCertChain(new Certificate[] { cert })
//        storeCertificate(file, entry)
//      }
//
//      if (args[0].equals(LOAD_DER_PRIVATE_KEY) || args[0].equals(LOAD_DER_PRIVATE_KEY_SHORT)) {
//        String file = args[1]
//        PrivateKey key = loadPrivateKeyFromDER(new File(file))
//
//        System.out.println(key.toString())
//      }
//    } else {
//      printHelp()
//    }
//  }

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   *
   * @throws CertificateException
   * @throws IOException
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   */
  def parseCertificate(data: Reader ):CertificateEntry = {
    val br = new BufferedReader(data)
    var sb = new StringBuilder(4096)
    val certs = new ArrayList[X509Certificate]()
    var privateKey:PrivateKey = null
    var line: String = null

    var addToBuffer = false

    while ((line = br.readLine()) != null) {

      if (line.contains(BEGIN_CERT) || line.contains(BEGIN_KEY) || line.contains(BEGIN_RSA_KEY)) {
        addToBuffer = true
      }
      else if (line.contains(END_CERT)) {
        addToBuffer = false
        val bytes = decoder.decodeBuffer(sb.toString())
        val bais = new ByteArrayInputStream(bytes)
        val cf = CertificateFactory.getInstance("X.509")

        while (bais.available() > 0) {
          val cert = cf.generateCertificate(bais)

          certs.add(cert.asInstanceOf[X509Certificate])
        }

        sb = new StringBuilder(4096)
      }
      else if (line.contains(END_KEY)) {
        addToBuffer = false
        val bytes = decoder.decodeBuffer(sb.toString())
        val keySpec = new PKCS8EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        privateKey = keyFactory.generatePrivate(keySpec)
        sb = new StringBuilder(4096)
      }
      else if (line.contains(END_RSA_KEY)) {
        addToBuffer = false
        val bytes = decoder.decodeBuffer(sb.toString())
        val keyDocoder = new RSAPrivateKeyDecoder(bytes)

        privateKey = keyDocoder.getPrivateKey()
        sb = new StringBuilder(4096)
      } else if (addToBuffer)
        sb.append(line)

    }

    val entry = new CertificateEntry()

    entry.chain = certs.toArray(new Array[Certificate](certs.size()))
    entry.privateKey = privateKey

    entry
  }

  def sort(chain: Array[Certificate]): Array[Certificate] = {
    val array = new ArrayList(util.Arrays.asList(chain: _*))
    val result = sort(array)
    result.toArray(new Array[Certificate](result.size()))
  }

  def sort(certs: List[Certificate]):List[Certificate] = {
    var rt:Certificate = null
    certs.foreach { x509Certificate =>
      val i = x509Certificate.asInstanceOf[X509Certificate].getIssuerDN()
      val s = x509Certificate.asInstanceOf[X509Certificate].getSubjectDN()
      if (i.equals(s))
        rt = x509Certificate
    }
    if (rt == null)
      throw new RuntimeException("Can't find root certificate in chain!")
    val res = new ArrayList[Certificate]()
    certs.remove(rt)
    res.add(rt)
    while (!certs.isEmpty()) {

      val subjectDN = rt.asInstanceOf[X509Certificate].getSubjectDN
      def parseOpt(cert: Certificate): Option[Certificate] = {
        if(cert.asInstanceOf[X509Certificate].getIssuerDN == subjectDN)
          Some(cert)
        else
          None
      }
      val found = certs.toList.view.flatMap(parseOpt).headOption
      if(found.isDefined) {
        rt = found.get
        certs.remove(rt)
        res.add(0, rt)
      }
      else {
        throw new RuntimeException("Can't find certificate " + rt.asInstanceOf[X509Certificate].getSubjectDN()
          + " in chain. Verify that all entries are correct and match against each other!")
      }
    }
    res
  }


//  private static void printHelp() {
//    System.out.println(CertificateUtil.class.getName() + " test code.")
//    System.out.println("You can run following tests:")
//    System.out.println(" " + PRINT_PROVIDERS + " | " + PRINT_PROVIDERS_SHORT + " - prints all supported providers")
//    System.out.println(" " + PRINT_SERVICES + " | " + PRINT_SERVICES_SHORT + " - print all supported services")
//    System.out.println(" " + KEY_PAIR + " | " + KEY_PAIR_SHORT + " - generate a key pair and print the result")
//    System.out.println(" " + ENCRIPT_TEST + " | " + ENCRIPT_TEST_SHORT
//      + " - encript simple text with public key, decript with private")
//    System.out.println(" " + SELF_SIGNED_CERT + " | " + SELF_SIGNED_CERT_SHORT + " - generate self signed certificate")
//    System.out.println(" " + LOAD_CERT + " file.pem | " + LOAD_CERT_SHORT + " file.pem - load certificate from file")
//    System.out.println(" " + STORE_CERT + " file.pem | " + STORE_CERT_SHORT
//      + " file.pem - generate self-signed certificate and save it to the given pem file")
//    System.out.println(" " + LOAD_DER_PRIVATE_KEY + " | " + LOAD_DER_PRIVATE_KEY_SHORT
//      + " file.der - load private key from DER file.")
//  }

//  /**
//   * Method description
//   *
//   *
//   * @param includeServices
//   */
//  private static void printProviders(boolean includeServices) {
//
//    // Initialization, basic information
//    Provider[] providers = Security.getProviders()
//
//    if ((providers != null) && (providers.length > 0)) {
//      for (Provider provider : providers) {
//        System.out.println(provider.getName() + "\t" + provider.getInfo())
//
//        if (includeServices) {
//          for (Provider.Service service : provider.getServices()) {
//            System.out.println("\t" + service.getAlgorithm())
//          }
//        }
//      }
//    } else {
//      System.out.println("No security providers found!")
//    }
//  }
//
//  private static void selfSignedCertTest() throws Exception {
//    KeyPair keyPair = createKeyPair(1024, "secret")
//
//    // Certificate
//    String email = "artur.hefczyc@tigase.org"
//    String domain = "tigase.org"
//    String ou = "XMPP Service"
//    String o = "Tigase.org"
//    String l = "Cambourne"
//    String st = "Cambridgeshire"
//    String c = "UK"
//
//    System.out.println("Creating self-signed certificate for issuer: " + domain)
//
//    X509Certificate cert = createSelfSignedCertificate(email, domain, ou, o, l, st, c, keyPair)
//
//    System.out.print("Checking certificate validity today...")
//    System.out.flush()
//    cert.checkValidity()
//    System.out.println(" done.")
//    System.out.print("Checking certificate validity yesterday...")
//    System.out.flush()
//
//    try {
//      cert.checkValidity(new Date(System.currentTimeMillis() - (1000 * 3600 * 24)))
//      System.out.println(" error.")
//    } catch (CertificateNotYetValidException e) {
//      System.out.println(" not valid!")
//    }
//
//    System.out.print("Verifying certificate with public key...")
//    System.out.flush()
//    cert.verify(keyPair.getPublic())
//    System.out.println(" done.")
//    System.out.println(cert.toString())
//  }

  /**
   * Method description
   *
   *
   * @param file
   * @param entry
   *
   * @throws CertificateEncodingException
   * @throws IOException
   */
  def storeCertificate(file: String, entry: CertificateEntry ) {
    val pemFormat = exportToPemFormat(entry)

    val f = new File(file)
    if (f.exists())
      f.renameTo(new File(file + ".bak"))

    val fw = new FileWriter(f, false)

    fw.write(pemFormat)
    fw.close()
  }

  /**
   * Method description
   *
   *
   *
   * @param chain
   * @param revocationEnabled
   *
   * @param trustKeystore
   * @return
   *
   *
   * @throws CertificateException
   * @throws NoSuchAlgorithmException
   * @throws KeyStoreException
   * @throws InvalidAlgorithmParameterException
   */
  def validateCertificate(chain: Array[Certificate], trustKeystore: KeyStore , revocationEnabled: Boolean): CertCheckResult ={
    val certPathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType())
    val selector = new X509CertSelector()
    val params = new PKIXBuilderParameters(trustKeystore, selector)

    params.setRevocationEnabled(false)

    val certPath = CertificateFactory.getInstance("X.509").generateCertPath(chain.toList)

    try {
      certPathValidator.validate(certPath, params)

      CertCheckResult.trusted
    }
    catch  {
      case ex: CertPathValidatorException =>
      if (isExpired(chain(0).asInstanceOf[X509Certificate])) {
        return CertCheckResult.expired
      }

      if ((chain.length == 1) && isSelfSigned(chain(0).asInstanceOf[X509Certificate])) {
        return CertCheckResult.self_signed
      } else {
        return CertCheckResult.untrusted
      }
    }
  }
}

object CertCheckResult extends Enumeration {
  type CertCheckResult = Value
  val trusted, self_signed, untrusted, revoked, expired, invalid, none = Value
}