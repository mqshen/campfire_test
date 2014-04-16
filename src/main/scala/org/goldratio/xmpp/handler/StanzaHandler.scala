package org.goldratio.xmpp.handler

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import org.goldratio.xmpp.protocol.Stanza

/**
 * Created by GoldRatio on 4/8/14.
 */
@Sharable
class StanzaHandler extends SimpleChannelInboundHandler[Stanza]{

  def messageReceived(ctx: ChannelHandlerContext , stanza: Stanza) {
    System.out.print(stanza)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext , e: Throwable ) {
  }

}
