package org.goldratio.xmpp.network

import io.netty.channel.{ChannelFutureListener, Channel}
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLContext
import io.netty.handler.ssl.SslHandler
import org.goldratio.xmpp.ssl.SSLSocketChannelFactory
import org.goldratio.xmpp.protocol.{JID, Stanza, SaslAuth, Packet}

/**
 * Created by GoldRatio on 4/9/14.
 */
abstract class AbstractConnection(val sessionId: String)  {

  val disconnected = new AtomicBoolean()

  def disconnect() {
    onChannelDisconnect()
  }

  def onChannelDisconnect() {
    disconnected.set(true)
  }

  def send(packet:Packet)

  def auth(auth: SaslAuth): Option[JID]

  def sendStartMessage()

  def process(stanza: Stanza)

  def secure()
}
