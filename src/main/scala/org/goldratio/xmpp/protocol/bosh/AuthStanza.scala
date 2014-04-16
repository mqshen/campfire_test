package org.goldratio.xmpp.protocol.bosh

import org.goldratio.xmpp.protocol.{Packet, XmlWrapper}
import scala.xml.Node

/**
 * Created by GoldRatio on 4/15/14.
 */
class AuthStanza(xml:Node) extends XmlWrapper(xml) with Packet {

}
