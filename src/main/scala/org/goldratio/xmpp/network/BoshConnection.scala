package org.goldratio.xmpp.network

import io.netty.channel.{ChannelFutureListener, Channel}
import java.util.UUID
import org.goldratio.xmpp.protocol._
import org.goldratio.xmpp.protocol.message.Message
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpHeaders.Names._
import scala.xml
import scala.xml.{Node, MinimizeMode}
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import org.goldratio.xmpp.protocol.bosh.{BodyPacker, BoshPacket}
import org.goldratio.xmpp.handler.BoshTransport
import org.goldratio.xmpp.scheduler.CancelableScheduler
import java.util.concurrent.TimeUnit

/**
 * Created by GoldRatio on 4/15/14.
 */
class BoshConnection(channel: Channel,
                    sessionId: String,
                     saslProcsess: SASLProcessor,
                     processor: Processor,
                      rid: String,
                      scheduler: CancelableScheduler)
  extends AbstractConnection(sessionId) with Transport {


  scheduleNoop(sessionId)


  def scheduleNoop(key: String) {
    scheduler.cancel(key);
    scheduler.schedule(key, new Runnable() {
      override def run() {
        BoshConnection.this.send("")
      }
    }, 30, TimeUnit.SECONDS);
  }


  override def handler(msg: Message): Unit = {

  }

  override def send(string: String): Unit = {
    BoshTransport.send(this.channel, "", rid)
  }

  override def send(packet: Node): Unit = {

  }

  override def secure(): Unit = {

  }

  override def process(stanza: Stanza): Unit = {

  }

  override def sendStartMessage(): Unit = {

  }

  override def auth(auth: SaslAuth): Option[JID] = {
    null
  }

  override def send(packet: Packet): Unit = {
    packet match {
      case stanza: Stanza =>
        BoshTransport.send(this.channel, stanza.xml, rid)
    }
  }
}
