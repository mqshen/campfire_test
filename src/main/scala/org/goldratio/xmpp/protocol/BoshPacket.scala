package org.goldratio.xmpp.protocol.bosh

import scala.xml.{TopScope, Null, Elem, Node}
import scala.collection.Seq
import org.goldratio.core.Constance
import org.goldratio.xmpp.protocol._


/**
 * Created by GoldRatio on 4/15/14.
 */
object BodyPacker {
  def apply(ack: String, features:Seq[Node]):Node = {
    <body  host="localhost"
           xmlns:stream="http://etherx.jabber.org/streams"
           from={Constance.domain}
           xmlns="http://jabber.org/protocol/httpbind"
           secure="true"
           ack={ack}
           xmpp:version="1.0"
           xmlns:xmpp="urn:xmpp:xbosh">{features}</body>
  }


  def apply(ack: String, str: String):Node = {
    <body  host="localhost"
           xmlns:stream="http://etherx.jabber.org/streams"
           from={Constance.domain}
           xmlns="http://jabber.org/protocol/httpbind"
           secure="true"
           ack={ack}
           xmpp:version="1.0"
           xmlns:xmpp="urn:xmpp:xbosh">{str}</body>
  }
}

case class BoshStreamStart(rid: String) extends Packet {
}

case class BoshStramInit(rid: String, sid: String) extends Packet{
}

case class BoshRequest(packet: BoshPacket, special: Packet) extends Packet{
}

object BoshPacket {

  def apply(xml:Node): Packet = {
    val rid = (xml \ "@rid").text
    if((xml \ "@sid").isEmpty)
      new BoshStreamStart(rid)
    else {
      val sid = (xml \ "@sid").text
      val children = xml.child
      if(children.isEmpty)
        new BoshStramInit(rid, sid)
      else {
        val rid = (xml \ "@rid").text
        val from:Option[JID] = (xml \ "@from").text
        val packet = new BoshPacket(rid, sid, from)
        val node = children(0)
        val special = node.label match {
          case SaslAuth.tag =>
            SaslAuth(node)
          case SaslSuccess.tag =>
            SaslSuccess(node)
          case SaslAbort.tag =>
            SaslAbort(node)
          case SaslError.tag =>
            SaslError(node)
          case Features.tag =>
            Features(node)
          case Handshake.tag =>
            Handshake(node)
          case StreamError.tag =>
            StreamError(node)
          case _ =>
            Stanza(node)
        }
        new BoshRequest(packet, special)
      }
    }
  }

  private implicit def string2optjid(string:String):Option[JID] = if (null != string && !string.isEmpty) Some(JID(string)) else None

}

case class BoshSaslAuth(bare: BoshPacket, auth: SaslAuth) extends Packet

case class BoshPacket(rid: String, sid: String, from: Option[JID]) extends Packet
{

}
