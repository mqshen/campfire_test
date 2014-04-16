package org.goldratio.xmpp.protocol.extensions.ping

import org.goldratio.xmpp.protocol.extensions.vcard.Builder
import scala.xml.Node
import org.goldratio.xmpp.protocol.Extension

/**
 * Created by GoldRatio on 4/14/14.
 */
object Ping
{
  val tag = "ping"

  def apply():Ping = apply(<ping xmlns={ Builder.namespace }/>)

  def apply(xml:Node):Ping = new Ping(xml)
}

class Ping(xml:Node) extends Extension(xml)
