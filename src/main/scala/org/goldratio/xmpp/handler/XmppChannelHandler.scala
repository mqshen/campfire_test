package org.goldratio.xmpp.handler

import io.netty.channel.{Channel, ChannelHandlerContext, ChannelHandlerAdapter}
import org.goldratio.xmpp.protocol._
import io.netty.channel.ChannelHandler.Sharable
import org.goldratio.xmpp.network.{AbstractConnection, Disconnectable, XmppConnection}
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import org.goldratio.xmpp.protocol.message.Message

/**
  * Created by GoldRatio on 4/8/14.
  */

@Sharable
class XmppChannelHandler(saslProcsess: SASLProcessor,
                         processor: Processor,
                         disconnect: Disconnectable,
                         connectionManager: ConnectionManager) extends ChannelHandlerAdapter {

  //val sessionId2Connection = new ConcurrentHashMap[String, AbstractConnection]()
  val channelId2Connection = new ConcurrentHashMap[Channel, AbstractConnection]()

  def connectStart(channel: Channel) = {
    val connection = channelId2Connection.get(channel)
    if(connection == null) {
      val sessionId = UUID.randomUUID()
      val connection = new XmppConnection(channel, sessionId.toString, connectionManager, disconnect, saslProcsess, processor)
      channelId2Connection.put(channel, connection)
    }
    else {
      connection.sendStartMessage()
    }
  }

  def connectEnd(channel: Channel) = {
    val connection = channelId2Connection.remove(channel)
    //if(connection != null)
    //  sessionId2Connection.remove(connection.sessionId)
    channel.close()
  }


  def startTls(channel: Channel) = {
    val connection = channelId2Connection.get(channel)
    if(connection != null) {
      connection.send(TlsProceed())
      connection.secure()
    }
    else {
      channel.close()
    }
  }

  def saslAuth(channel: Channel, auth: SaslAuth) = {
    val connection = channelId2Connection.get(channel)
    val result = connection.auth(auth)
    result match {
      case Some(jid) =>
        connectionManager.addConnection(jid, connection)
 //       sessionId2Connection.put(jid.bare.toString, connection)
      case _ =>
        connection.disconnect
    }
  }

  def processStanza(channel: Channel, stanza: Stanza) = {
    val connection = channelId2Connection.get(channel)
    if(connection != null) {
      connection.process(stanza)
    }
    else {
      channel.close()
    }
  }

  override def channelRead(ctx: ChannelHandlerContext , obj: Any) {
     obj match {
       case msg: StreamStart =>
         connectStart(ctx.channel())
       case msg: StreamEnd =>
         connectEnd(ctx.channel())
       case msg: Stanza =>
         processStanza(ctx.channel(), msg)
       case msg: StartTls =>
         startTls(ctx.channel())
       case msg: SaslAuth =>
         saslAuth(ctx.channel(), msg)
     }
  }


}


