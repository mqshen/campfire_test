package org.goldratio.xmpp.start

import scala.collection.mutable
import sun.misc.Resource
import scala.xml.{XML, Node}
import java.io.{FileInputStream, File}

/**
 * Created by GoldRatio on 4/10/14.
 */
class XmlConfiguration() {
  val processor = new JettyXmlConfiguration(this)

  val idMap = new mutable.HashMap[String, AnyRef]()

  def process(node: Node) {
    processor.configure(node: Node)
    //System.out.print(idMap)
  }

  def process(file: String) {
    val xml = XML.loadFile(file)
    process(xml)
  }

}

object XmlMain{
  def main(args: Array[String]) {
    val conf = new XmlConfiguration()
    conf.process("database.xml")
    conf.process("main.xml")
  }
}

