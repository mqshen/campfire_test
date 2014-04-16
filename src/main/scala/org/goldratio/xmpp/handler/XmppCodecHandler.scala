package org.goldratio.xmpp.handler

import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.channel.ChannelHandlerContext
import io.netty.buffer.ByteBuf
import java.util
import scala.collection.{mutable, Seq}
import scala.xml._
import scala.xml.pull._
import io.netty.util.internal.AppendableCharSequence
import org.goldratio.xmpp.protocol._
import scala.xml.pull.EvElemStart
import scala.xml.NamespaceBinding
import scala.xml.EntityRef
import scala.xml.pull.EvText
import scala.xml.pull.EvEntityRef
import scala.xml.pull.EvElemEnd
import scala.Some
import scala.xml.Comment
import scala.xml.Text
import scala.xml.pull.EvProcInstr
import scala.xml.pull.EvComment
import scala.xml.ProcInstr

/**
 * Created by GoldRatio on 4/8/14.
 */
class XmppCodecHandler extends ByteToMessageDecoder {
  val sb = new AppendableCharSequence(128)

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    var str = read(in)
    System.out.println("receive string:" + str)
    if (str.startsWith("<?xml")) {
      str = str.substring(str.indexOf(">") + 1)
    }
    str = str.trim()
    if(str.startsWith("<stream:stream"))
      out.add(new StreamStart)
    else if(str.equals("</stream:stream>")) {
      out.add(new StreamEnd)
    }
    else {
      XmlParser.parseXml(str) match {
        case Some(xmls) => {
          xmls.map( xml => {
            xml.namespace match {
              case Tls.namespace => xml.label match {
                case StartTls.tag =>
                  out.add(StartTls(xml))
                case TlsProceed.tag =>
                  out.add(TlsProceed(xml))
                case TlsFailure.tag =>
                  out.add(TlsFailure(xml))
                case _ =>
                  throw new Exception("unknown tls packet %s".format(xml.label))
              }
              case Sasl.namespace => xml.label match {
                case SaslAuth.tag =>
                  out.add(SaslAuth(xml))
                case SaslSuccess.tag =>
                  out.add(SaslSuccess(xml))
                case SaslAbort.tag =>
                  out.add(SaslAbort(xml))
                case SaslError.tag =>
                  out.add(SaslError(xml))
                case _ =>
                  throw new Exception("unknown sasl packet %s".format(xml.label))
              }
              case _ => xml.label match {
                case Features.tag =>
                  out.add(Features(xml))
                case Handshake.tag =>
                  out.add(Handshake(xml))
                case StreamError.tag =>
                  out.add(StreamError(xml))
                case _ =>
                  out.add(Stanza(xml))
              }
            }
          })
        }
        case None => {
        }
      }
    }
  }

  private def read(buffer: ByteBuf): String = {
    val total = buffer.readableBytes()
    val bytes = new Array[Byte](total)
    buffer.readBytes(bytes)
    if(total > 0) {
      new String(bytes)
    }
    else {
      ""
    }
  }


}
