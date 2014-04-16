import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler, ChannelInitializer}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}

/**
 * Created by GoldRatio on 4/8/14.
 */
class XmppClientInitializer extends ChannelInitializer[SocketChannel] {
  val handler = new XmppClientHandler
  val DECODER = new StringDecoder()
  val ENCODER = new StringEncoder()

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()

    pipeline.addLast("decoder", DECODER)
    pipeline.addLast("encoder", ENCODER)

    pipeline.addLast("handler", handler)
  }
}

@Sharable
class XmppClientHandler extends SimpleChannelInboundHandler[String] {


  override def messageReceived(ctx: ChannelHandlerContext , msg: String) {
    System.err.println(msg)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext , cause: Throwable ) {
    ctx.close()
  }
}