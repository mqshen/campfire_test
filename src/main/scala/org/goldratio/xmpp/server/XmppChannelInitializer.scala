package org.goldratio.xmpp.server

import io.netty.channel.{Channel, ChannelInitializer}
import org.goldratio.xmpp.handler._
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.goldratio.xmpp.network.{XmppConnectionManager, XmppConnection, AbstractConnection, Disconnectable}
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}
import org.goldratio.xmpp.protocol.Stanza
import io.netty.handler.codec.http.{HttpResponseEncoder, HttpObjectAggregator, HttpRequestDecoder}
import org.goldratio.xmpp.scheduler.CancelableScheduler

/**
 * Created by GoldRatio on 4/8/14.
 */
class XmppChannelInitializer(saslProcsess: SASLProcessor, processor: Processor,connectionManager: ConnectionManager) extends ChannelInitializer[Channel] with Disconnectable {


  val xmppChannelHander = new XmppChannelHandler(saslProcsess, processor, this, connectionManager)
  val stanzaHandler = new StanzaHandler
  val stringEncoder = new StringEncoder()

  override def initChannel(ch: Channel) {
    val pipeline = ch.pipeline()

    pipeline.addLast(new XmppCodecHandler)

    pipeline.addLast(xmppChannelHander)

    pipeline.addLast(stanzaHandler)

    pipeline.addLast(stringEncoder)
  }

  override def onDisconnect(client: AbstractConnection): Unit = {

  }
}

class BoshChannelInitializer(saslProcsess: SASLProcessor,
                             processor: Processor,
                             connectionManager: ConnectionManager,
                              schedule: CancelableScheduler) extends ChannelInitializer[Channel] with Disconnectable {

  //val xmppChannelHander = new XmppChannelHandler(saslProcsess, processor, this)
  val boshCodecHandler = new BoshCodecHandler()
  val boshChannelHandler = new BoshChannelHandler(saslProcsess, processor, connectionManager, schedule)

  override def initChannel(ch: Channel) {
    val pipeline = ch.pipeline()

    pipeline.addLast("decoder", new HttpRequestDecoder())

    pipeline.addLast("aggregator", new HttpObjectAggregator(64 * 1024))

    pipeline.addLast("encoder", new HttpResponseEncoder())

    pipeline.addLast(boshCodecHandler)

    pipeline.addLast(boshChannelHandler)

//    pipeline.addLast(xmppChannelHander)
//
//    pipeline.addLast(stanzaHandler)
//
//    pipeline.addLast(stringEncoder)
  }

  override def onDisconnect(client: AbstractConnection): Unit = {

  }

}
