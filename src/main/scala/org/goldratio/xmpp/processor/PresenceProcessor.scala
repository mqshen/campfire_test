package org.goldratio.xmpp.processor

import org.goldratio.xmpp.protocol.Stanza
import org.goldratio.xmpp.network.Transport
import org.goldratio.xmpp.protocol.iq.{Set, Get}

/**
 * Created by GoldRatio on 4/15/14.
 */
class PresenceProcessor extends Processor("presence") {
  override def processorSet(set: Set, transport: Transport): Unit = {

  }

  override def processorGet(get: Get, transport: Transport): Unit = {

  }

  override def process(packet: Stanza, transport: Transport): Unit = {
    transport.send(<presence from={transport.jid.get.toString} xmlns="jabber:client" to={transport.jid.get.bare.toString}/> )
  }
}
