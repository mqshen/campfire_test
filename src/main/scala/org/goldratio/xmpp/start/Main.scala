package org.goldratio.xmpp.start

import org.goldratio.xmpp.network.XmppConnectionManager
import org.goldratio.xmpp.server.{NettyBoshServer, NettyXmppServer}
import org.goldratio.xmpp.processor.{Processor, SASLProcessor}

/**
 * Created by GoldRatio on 4/10/14.
 */
object Main {

  def main(args: Array[String]) = {
    val conf = new XmlConfiguration()
    val connectionManager = new XmppConnectionManager()
    conf.process("database.xml")
    conf.process("main.xml")
    val saslProcessor = conf.idMap.get("saslProcessor")
    val processor = conf.idMap.get("processor")
    if(saslProcessor.isDefined && processor.isDefined){
      val xmppServer = new NettyXmppServer(saslProcessor.get.asInstanceOf[SASLProcessor],
        processor.get.asInstanceOf[Processor],
        connectionManager)

      val boshServer = new NettyBoshServer(saslProcessor.get.asInstanceOf[SASLProcessor],
        processor.get.asInstanceOf[Processor],
        connectionManager)


      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run {
          xmppServer.stop()
          boshServer.stop()
        }
      })
    }
    else {
      throw new Exception("saslProcessor not found")
    }
  }

}
