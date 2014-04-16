package org.goldratio.xmpp.handler

import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import io.netty.channel.{ChannelFutureListener, Channel, ChannelHandlerContext, ChannelHandlerAdapter}
import org.goldratio.xmpp.protocol.bosh._
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpHeaders.Names._
import java.util.UUID
import scala.xml
import io.netty.channel.ChannelHandler.Sharable
import org.goldratio.xmpp.protocol._
import org.goldratio.xmpp.protocol.bosh.BoshRequest
import org.goldratio.xmpp.protocol.bosh.BoshStreamStart
import org.goldratio.xmpp.protocol.bosh.BoshStramInit
import scala.xml.Node
import io.netty.util.internal.ConcurrentSet
import org.goldratio.xmpp.network.{BoshConnection, Transport}
import org.goldratio.xmpp.protocol.message.Message
import java.util.concurrent.ConcurrentHashMap
import org.goldratio.xmpp.scheduler.CancelableScheduler

/**
 * Created by GoldRatio on 4/15/14.
 */
object BoshTransport {

  def send(channel: Channel, result: String) {
    val response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(result.getBytes))

    response.headers().set(CONTENT_TYPE, "text/plain")
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes())
    response.headers().set("Access-Control-Allow-Headers", "Content-Type")
    response.headers().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
    response.headers().set("Access-Control-Allow-Origin", "*")

    val f = channel.writeAndFlush(response)
    f.addListener(ChannelFutureListener.CLOSE)
  }

  def send(channel: Channel, node: Node, rid: String) {
    val result = BodyPacker(rid, node)
    BoshTransport.send(channel, result.toString)
  }

  def send(channel: Channel, str: String, rid: String) {
    val result = BodyPacker(rid, str)
    BoshTransport.send(channel, result.toString())
  }

  def send(channel: Channel, node: Node, packet: BoshPacket) {
    val result = BodyPacker(packet.rid, node)
    BoshTransport.send(channel, result.toString)
  }

  def apply(channel: Channel, boshPacket: BoshPacket, jid: Option[JID], connectionManage: ConnectionManager): BoshTransport = {
    val result = new BoshTransport(channel, boshPacket, connectionManage)
    result.jid = jid
    result
  }


}
class BoshTransport(channel: Channel, boshPacket: BoshPacket, connectionManage: ConnectionManager) extends Transport {

  override def handler(msg: Message): Unit = {
    val result = BodyPacker(boshPacket.rid, "")
    BoshTransport.send(channel, result.toString())
    connectionManage.handler(msg)
  }

  override def send(string: String): Unit = {
  }

  override def send(packet: Node): Unit = {
    val result = BodyPacker(boshPacket.rid, packet)
    BoshTransport.send(channel, result.toString())
  }

}

case class ConnectionStatus(jid: JID,var status: Int)
@Sharable
class BoshChannelHandler(saslProcsess: SASLProcessor, processor: Processor, connectionManage: ConnectionManager, schedule: CancelableScheduler) extends ChannelHandlerAdapter {

  val session = new ConcurrentHashMap[String, ConnectionStatus]()

  def connectStart(channel: Channel, msg: BoshStreamStart  ) = {
    val sessionId = UUID.randomUUID()
    val result = "<body maxpause=\"10\" ver=\"1.6\" xmlns=\"http://jabber.org/protocol/httpbind\" " +
      "secure=\"true\" ack=\"" + msg.rid + "\" xmlns:xmpp=\"urn:xmpp:xbosh\" " +
      "polling=\"10\" authid=\"" + sessionId.toString + "\" " +
      "host=\"localhost\" xmlns:stream=\"http://etherx.jabber.org/streams\" " +
      "inactivity=\"10\" from=\"mytask.com\" wait=\"30\" hold=\"1\" requests=\"2\" " +
      "sid=\"" + sessionId.toString + "\" xmpp:version=\"1.0\"/>"

    BoshTransport.send(channel, result)
  }

  def connectInit(channel: Channel, init: BoshStramInit): Unit = {
    val sessionId = init.sid
    if(!sessionId.isEmpty ){
      val connectionStatus = session.get(sessionId)
      if(connectionStatus != null) {

        if(connectionStatus.status == 0) {
          val result = BodyPacker(init.rid, <stream:features xmlns="jabber:client">
            <bind xmlns="urn:ietf:params:xml:ns:xmpp-bind"/>
          </stream:features>)
          connectionStatus.status = 1
          //session.put(sessionId, ConnectionStatus(connectionStatus.jid, 0))
          BoshTransport.send(channel, xml.Utility.trim(result).toString)
        }
        else {
          val conn = new BoshConnection(channel, sessionId, saslProcsess, processor, init.rid, schedule)
          connectionManage.addConnection(connectionStatus.jid, conn)
        }
      }
      else {
        val result = BodyPacker(init.rid, <stream:features xmlns="jabber:client">
          <mechanisms xmlns="urn:ietf:params:xml:ns:xmpp-sasl">
            <mechanism>PLAIN</mechanism>
          </mechanisms>
        </stream:features>)

        BoshTransport.send(channel, xml.Utility.trim(result).toString)
      }
    }
    else {

      val result = BodyPacker(init.rid, <stream:features xmlns="jabber:client">
        <mechanisms xmlns="urn:ietf:params:xml:ns:xmpp-sasl">
          <mechanism>PLAIN</mechanism>
        </mechanisms>
      </stream:features>)

      BoshTransport.send(channel, xml.Utility.trim(result).toString)
    }

  }


  def auth(channel: Channel, auth: SaslAuth, packet: BoshPacket): Option[JID] = {
    val result = saslProcsess.process(auth)
    if(result.isDefined){
      session.put(packet.sid, ConnectionStatus(result.get, 0))
      BoshTransport.send(channel, SaslSuccess().xml, packet)
    }
    else
      BoshTransport.send(channel, SaslError(SaslErrorCondition.InvalidAuthzid).xml, packet)
    result
  }

  def processStanza(channel: Channel, stanza: Stanza, packet: BoshPacket) = {
    val jid = session.get(packet.sid).jid
    processor.process(stanza, BoshTransport(channel, packet, Some(jid), connectionManage))
  }

  override def channelRead(ctx: ChannelHandlerContext , obj: AnyRef) {
    obj match {
      case msg: BoshStreamStart =>
        connectStart(ctx.channel(), msg)
      case msg:BoshStramInit =>
        connectInit(ctx.channel(), msg)
      case msg:BoshRequest => {
        msg.special match {
          case saslAuth: SaslAuth =>
            auth(ctx.channel(), saslAuth, msg.packet)
          case stanza: Stanza =>
            processStanza(ctx.channel(), stanza, msg.packet)

        }
      }
//      case msg: StreamEnd =>
//        connectEnd(ctx.channel())
//      case msg: Stanza =>
//        processStanza(ctx.channel(), msg)
//      case msg: StartTls =>
//        startTls(ctx.channel())
//      case msg: SaslAuth =>
//        saslAuth(ctx.channel(), msg)
    }
  }




}
