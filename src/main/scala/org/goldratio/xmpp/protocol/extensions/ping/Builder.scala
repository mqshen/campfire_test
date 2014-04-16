package org.goldratio.xmpp.protocol.extensions.ping

import org.goldratio.xmpp.protocol.ExtensionBuilder
import scala.xml.Node

/**
 * Created by GoldRatio on 4/13/14.
 */
private[xmpp] object Builder extends ExtensionBuilder[Ping]
{
  val tag = Ping.tag
  val namespace = "urn:xmpp:ping"

  def apply(xml:Node):Ping = Ping(xml)
}
