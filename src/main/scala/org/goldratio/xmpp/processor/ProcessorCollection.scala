package org.goldratio.xmpp.processor

import scala.collection.mutable.HashMap
import org.goldratio.xmpp.protocol.{Stanza, Packet}
import org.goldratio.xmpp.network.Transport
import org.goldratio.xmpp.protocol.iq.{Set, Get, IQ}
import org.goldratio.xmpp.protocol.message.Message
import org.goldratio.xmpp.protocol.presence.Presence

/**
 * Created by GoldRatio on 4/12/14.
 */
class ProcessorCollection extends Processor("packet handler"){

  val processors = new HashMap[String, Processor]

  def setProcessor(processor: Processor) {
    processors.put(processor.name, processor)
  }

  override def process(stanza: Stanza, transport: Transport) = {
    val name = stanza match {
      case iq: IQ =>
        "iq"
      case message: Message=>
        "message"
      case presence: Presence=>
        "presence"
      case _ =>
        ""
    }
    val processor = processors.get(name)
    if(processor.isDefined) {
      processor.get.process(stanza, transport)
    }
  }

  override def processorSet(set: Set, transport: Transport) = {

  }

  override def processorGet(get: Get, transport: Transport) = {

  }
}
