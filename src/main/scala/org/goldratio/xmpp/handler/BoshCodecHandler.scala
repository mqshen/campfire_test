package org.goldratio.xmpp.handler

import io.netty.channel.{Channel, ChannelFutureListener, ChannelHandlerContext, ChannelHandlerAdapter}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, CloseWebSocketFrame}
import io.netty.handler.codec.http.{DefaultFullHttpResponse, QueryStringDecoder, FullHttpRequest}
import io.netty.channel.ChannelHandler.Sharable
import io.netty.buffer.{Unpooled, ByteBuf}
import org.goldratio.xmpp.protocol._

import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import java.util.UUID
import org.goldratio.xmpp.network.{AbstractConnection, BoshConnection, XmppConnection}
import scala.Some
import java.util.concurrent.ConcurrentHashMap
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import org.goldratio.xmpp.protocol.bosh.BoshPacket


/**
 * Created by GoldRatio on 4/15/14.
 */
@Sharable
class BoshCodecHandler extends ChannelHandlerAdapter {

  //val sessionId2Connection = new ConcurrentHashMap[String, BoshConnection]()

  override def channelReadComplete(ctx: ChannelHandlerContext ) {
    ctx.flush()
  }


  override def channelRead(ctx: ChannelHandlerContext , obj: AnyRef) {
    obj match {
      case req: FullHttpRequest =>
        val content = req.content()
        val str = read(content)
        System.out.println(str)
        XmlParser.parseXml(str) match {
          case Some(xmls) => {
            xmls.map( xml => {
              val packet = BoshPacket(xml)
              ctx.fireChannelRead(packet)
            })
//              if(packet.sid.isEmpty) {
//                val sessionId = UUID.randomUUID()
//                val connection = new BoshConnection(sessionId, saslProcsess, processor)
//                sessionId2Connection.put(sessionId.toString, connection)
//                connection.init(channel, packet)
//              }
//              else {
//                val connection = sessionId2Connection.get(packet.sid)
//                if(connection != null) {
//                  val children = packet.child
//                  if(children.size == 0) {
//                    connection.start(channel, packet)
//                  }
//                  else {
//                    children.foreach {child =>
//                      child.label match {
//                        case SaslAuth.tag =>
//                          connection.auth(SaslAuth(child))
//                        case _ =>
//
//                      }
//                    }
//                  }
//                }
//                else
//                  channel.disconnect()
          }
          case _ =>

        }
      case _ =>
        ctx.fireChannelRead(obj)
    }
  }

  private def read(buffer: ByteBuf): String = {
    val total = buffer.readableBytes()
    val bytes = new Array[Byte](total)
    buffer.readBytes(bytes)
    if(total > 0) {
      new String(bytes)
    }
    else {
      ""
    }
  }

}
