package org.goldratio.xmpp.processor

import org.goldratio.xmpp.protocol.{Packet, Stanza}
import org.goldratio.xmpp.network.Transport
import org.goldratio.xmpp.protocol.iq

/**
 * Created by GoldRatio on 4/12/14.
 */
abstract class Processor(val name: String) {

  def process(packet: Stanza, transport: Transport)

  def processorGet(get: iq.Get, transport: Transport)

  def processorSet(set: iq.Set, transport: Transport)

}
