package org.goldratio.xmpp.protocol.extensions.vcard

/**
 * Created by GoldRatio on 4/13/14.
 */
import scala.xml._
import org.goldratio.xmpp.protocol.Extension

object VCard
{
  val tag = "vCard"

  def apply():VCard = apply(<vCard xmlns={ Builder.namespace }/>)

  def apply(xml:Node):VCard = new VCard(xml)
}

class VCard(xml:Node) extends Extension(xml)
