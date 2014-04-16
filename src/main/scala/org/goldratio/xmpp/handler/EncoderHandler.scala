package org.goldratio.xmpp.handler

import io.netty.channel.{ChannelFutureListener, Channel, ChannelHandlerContext, ChannelHandlerAdapter}
import io.netty.channel.ChannelHandler.Sharable
import io.netty.buffer.ByteBuf
import org.goldratio.xmpp.protocol.Stanza
import io.netty.handler.codec.http.LastHttpContent

/**
 * Created by GoldRatio on 4/8/14.
 */
@Sharable
class EncoderHandler extends ChannelHandlerAdapter{

  def sendMessage(stanza: Stanza, channel: Channel, out: ByteBuf) = {

    channel.write(stanza.toString)

    if (out.isReadable()) {
      channel.write(out)
    } else {
      out.release()
    }

    val f = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    f.addListener(ChannelFutureListener.CLOSE)
  }

  def write(msg: Stanza, ctx: ChannelHandlerContext , out: ByteBuf ) {
    val channel = ctx.channel()

    if (!channel.isActive())
      out.release()
    else {
      sendMessage(msg, channel, out)
    }

  }

}
