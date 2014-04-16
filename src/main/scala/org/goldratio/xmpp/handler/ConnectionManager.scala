package org.goldratio.xmpp.handler

import org.goldratio.xmpp.protocol.message.Message
import org.goldratio.xmpp.protocol.JID
import org.goldratio.xmpp.network.AbstractConnection

/**
 * Created by GoldRatio on 4/14/14.
 */
trait ConnectionManager {

  def handler(msg: Message)

  def addConnection(jid: JID, connection: AbstractConnection)

  def removeConnection(jid: JID)
}
