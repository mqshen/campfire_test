package org.goldratio.xmpp.network

import io.netty.channel.Channel
import java.util.UUID
import org.goldratio.xmpp.protocol._
import scala.collection.Seq
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import org.goldratio.xmpp.protocol.extensions.bind.BindBuilder
import org.goldratio.xmpp.protocol.message.Message
import org.goldratio.xmpp.handler.ConnectionManager
import org.goldratio.xmpp.ssl.SSLSocketChannelFactory
import io.netty.handler.ssl.SslHandler
import scala.xml.Node

/**
 * Created by GoldRatio on 4/9/14.
 */
object XmppConnection {
  val XMLNS = "jabber:client"
}
class XmppConnection(channel: Channel,
                     sessionId: String,
                     connectionManager: ConnectionManager,
                     disconnectable: Disconnectable,
                     saslProcsess: SASLProcessor,
                      processor: Processor)
  extends AbstractConnection(sessionId) with Transport {

  var _username: String = null

  var state = 0


  import XmppConnection._

  val startXmlStr = "<?xml version='1.0'?><stream:stream" + " xmlns='" +
    XMLNS + "'" + " xmlns:stream='http://etherx.jabber.org/streams'" + " from='" +
    "mytask.com" + "'" + " id='" + sessionId.toString + "'" + " version='1.0' xml:lang='en'>"

  channel.writeAndFlush(startXmlStr)

  sendFeatures(<starttls xmlns={ Tls.namespace }>
    <required/>
  </starttls>)

  override def send(packet: Packet): Unit = {
    System.out.println("write string:" + packet.toString)
    channel.writeAndFlush(packet.toString)
  }

  override def send(string: String) = {
    System.out.println("write string:" + string)
    channel.writeAndFlush(string)
  }

  def sendStartMessage() {
    channel.write(startXmlStr)
    if(state == 0) {
      sendFeatures(<mechanisms xmlns={ Sasl.namespace }>
        <mechanism>{ SaslMechanism.Plain.toString }</mechanism>
      </mechanisms>)
      state = 1
    }
    else {
      sendFeatures(<bind xmlns={ BindBuilder.namespace }>
        <required/>
      </bind>)
    }
  }

  //val saslProcsess = new SASLProcessor

  def sendFeatures(features:Seq[scala.xml.Node]) {
    send(Features(features))
  }

  override def auth(auth: SaslAuth): Option[JID] = {
    val result = saslProcsess.process(auth)
    if(result.isDefined){
      state = 2
      this.jid = result
      send(SaslSuccess())
    }
    else
      send(SaslError(SaslErrorCondition.InvalidAuthzid))
    result
  }

  private class SaslAuthenticationError(val reason:SaslErrorCondition.Value) extends Exception

  override def process(stanza: Stanza) = {
    processor.process(stanza, this)
  }

  override def handler(msg: Message): Unit = {
    connectionManager.handler(msg)
  }


  override def secure() {
    if (null == channel.pipeline().get("ssl")) {
      val engine = SSLSocketChannelFactory.build()
      engine.setUseClientMode(false)
      //engine.setEnabledCipherSuites(Array[String]("TLS_RSA_WITH_AES_128_CBC_SHA"))
      channel.pipeline().addFirst("ssl", new SslHandler(engine))
    }
    else {
      //logger.warn("netty client connection %s already secured".format(id.get))
    }
  }


  override def send(packet: Node): Unit = {
    System.out.println("write string:" + packet.toString)
    channel.writeAndFlush(packet.toString)
  }
}

class AuthenticationRequest(val username:String, val password:String)

object AuthenticationResult extends Enumeration
{
  type result = Value
  val Success, NotAuthorized, CredentialsExpired, AccountDisabled, MalformedRequest, Unknown = Value
}
