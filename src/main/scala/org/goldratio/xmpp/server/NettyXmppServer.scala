package org.goldratio.xmpp.server

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelOption
import java.net.InetSocketAddress
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import org.goldratio.xmpp.start.XmlConfiguration
import org.goldratio.xmpp.processor.{SASLProcessor, Processor}
import org.goldratio.xmpp.network.XmppConnectionManager
import org.goldratio.xmpp.handler.ConnectionManager

/**
 * Created by GoldRatio on 4/8/14.
 */
class NettyXmppServer(saslProcessor: SASLProcessor,
                      processor: Processor,
                      connectionManager: ConnectionManager) {

  val xmppChannelInitializer = {
    new XmppChannelInitializer(saslProcessor, processor, connectionManager)
  }

  private def newBootstrap = {
    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup()
    val b = new ServerBootstrap()

    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .option[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)
      .option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      .childHandler(xmppChannelInitializer)
    b
  }

  val allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

  val HTTP = {
    val bootstrap = newBootstrap
    //bootstrap.setPipelineFactory(new TornadoPipelineFactory)
    val channel = bootstrap.bind(new InetSocketAddress("localhost", 8090)).sync().channel()
    allChannels.add(channel)
    (bootstrap, channel)
  }

  def stop() {

    allChannels.close().awaitUninterruptibly()
    // Release the HTTP server
    //HTTP.foreach(_._1.releaseExternalResourcbes())
    HTTP._2.closeFuture().sync()
    HTTP._1.group().shutdownGracefully()
    HTTP._1.childGroup().shutdownGracefully()
  }
}
