package org.goldratio.xmpp.network

import org.goldratio.xmpp.protocol.{Packet, JID}
import org.goldratio.xmpp.protocol.message.Message
import scala.xml.Node

/**
 * Created by GoldRatio on 4/12/14.
 */
trait Transport {
  var jid: Option[JID] = None

  def setResource(resource: Option[String]) {
    if(resource.isDefined && jid.isDefined)
      this.jid = Some(this.jid.get.copyWithOutResource(resource.get))
  }

  def send(packet: Node)

  def send(string: String)

  def handler(msg: Message)
}
