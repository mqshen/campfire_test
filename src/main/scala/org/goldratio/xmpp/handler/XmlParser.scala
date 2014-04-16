package org.goldratio.xmpp.handler


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
 * Created by GoldRatio on 4/15/14.
 */
object XmlParser {

  def parseXml(input:String):Option[Seq[Node]] = {
    var level = 0
    val children = mutable.HashMap[Int, mutable.ListBuffer[Node]]()
    val attributes = mutable.HashMap[Int, MetaData]()
    val scope = mutable.HashMap[Int, NamespaceBinding]()
    val nodes = mutable.ListBuffer[Node]()

    try {
      // using a customized version of XMLEventReadr as it is buggy, see
      // http://scala-programming-language.1934581.n4.nabble.com/OutOfMemoryError-when-using-XMLEventReader-td2341263.html
      //  should be fixed in scala 2.9, need to test when it is released
      val tokenizer = new XMLEventReaderEx(scala.io.Source.fromString(input))
      tokenizer.foreach( token => {
        token match {
          case tag:EvText => children(level) += new Text(tag.text)
          case tag:EvProcInstr => children(level) += new ProcInstr(tag.target, tag.text)
          case tag:EvComment => children(level) += new Comment(tag.text)
          case tag:EvEntityRef => children(level) += new EntityRef(tag.entity)
          case tag:EvElemStart => {
            level += 1
            if (!attributes.contains(level)) attributes += level -> tag.attrs else attributes(level) = tag.attrs
            if (!scope.contains(level)) scope += level -> tag.scope else scope(level) = tag.scope
            if (!children.contains(level)) children += level -> new mutable.ListBuffer[Node]() else children(level) = new mutable.ListBuffer[Node]()
          }
          case tag:EvElemEnd => {
            val node = Elem(tag.pre, tag.label, attributes(level), scope(level), children(level):_*)

            level -= 1
            if (0 == level) {
              nodes += node
            }
            else {
              children(level) += node
            }
          }
        }
      })

      return if (nodes.length > 0) Some(nodes) else None
    }
    catch {
      // TODO: would be nice to handle bad vs. partial xml, only the latter is important to us (for buffering)
      case e:parsing.FatalError => None
      case e => throw e
    }
  }

}
