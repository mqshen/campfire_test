package org.goldratio.xmpp.protocol.extensions.vcard

import org.goldratio.xmpp.protocol.ExtensionBuilder
import scala.xml.Node

/**
 * Created by GoldRatio on 4/13/14.
 */
private[xmpp] object Builder extends ExtensionBuilder[VCard]
{
  val tag = VCard.tag
  val namespace = "vcard-temp"

  def apply(xml:Node):VCard = VCard(xml)
}
