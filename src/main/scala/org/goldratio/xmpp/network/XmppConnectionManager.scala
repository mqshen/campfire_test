package org.goldratio.xmpp.network

import java.util.concurrent.ConcurrentHashMap
import org.goldratio.xmpp.protocol.JID
import org.goldratio.xmpp.handler.ConnectionManager
import org.goldratio.xmpp.protocol.message.Message

/**
 * Created by GoldRatio on 4/16/14.
 */
class XmppConnectionManager extends ConnectionManager {

  val jid2Connection = new ConcurrentHashMap[JID, AbstractConnection]()

  def addConnection(jid: JID, connection: AbstractConnection) {
    jid2Connection.put(jid, connection)
  }

  def removeConnection(jid: JID) {
    val connection = jid2Connection.remove(jid)
    connection.disconnect()
  }

  override def handler(msg: Message): Unit = {
    val connection = jid2Connection.get(msg.to.get.bare)
    if(connection != null)
      connection.send(msg)
  }
}
