package org.goldratio.xmpp.start

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.collection.mutable.{Set, HashSet}

/**
 * Created by GoldRatio on 4/10/14.
 */
object StartArgs {
  def apply(cmdLine: Array[String]) = {
    val startArgs = new StartArgs
    startArgs.addCmdLine(cmdLine)
  }
}

class StartArgs {

  val commandLine: Buffer[String] = new ArrayBuffer[String]()
  val modules: Set[String] = new HashSet[String]()

  def addCmdLine(cmdLine: Array[String]) {
    commandLine.append(cmdLine:_*)
  }

}
