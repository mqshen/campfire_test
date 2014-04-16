import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.io.{InputStreamReader, BufferedReader}

/**
 * Created by GoldRatio on 4/8/14.
 */
class TelnetClient(host: String, port: Int) {

  def run() = {
    val group = new NioEventLoopGroup();
    try {
      val b = new Bootstrap();
      b.group(group)
        .channel(classOf[NioSocketChannel])
      .handler(new XmppClientInitializer())

      // Start the connection attempt.
      val ch = b.connect(host, port).sync().channel()

      // Read commands from the stdin.
      val lastWriteFuture = ch.writeAndFlush("<?xml version='1.0' ?><stream:stream to='mytask.com' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>")

      lastWriteFuture.sync()

      // If user typed the 'bye' command, wait until the server closes
      // the connection.

    } finally {
      group.shutdownGracefully();
    }
  }

}

object TelnetClient {
  def main(args: Array[String]) {
    val client = new TelnetClient("localhost", 8080)
    client.run()
  }
}