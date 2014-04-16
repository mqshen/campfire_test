package org.goldratio.xmpp.start

import java.io.File
import java.net.{URI, URL}
import java.util.regex.{Pattern, Matcher}

/**
 * Created by GoldRatio on 4/10/14.
 */
object BaseHome {

  def apply() = {
    val baseDir = new File(System.getProperty("campfire.base", System.getProperty("user.dir", ".")))
    val jarfile: URL = this.getClass.getClassLoader.getResource("org/goldratio/campfire/start/BaseHome.class")
    val homeDir: File =  if (jarfile != null) {
      val m: Matcher = Pattern.compile("jar:(file:.*)!/org/goldratio/xmpp/start/BaseHome.class").matcher(jarfile.toString)
      if (m.matches) {
        new File(new URI(m.group(1))).getParentFile
      }
      else {
        baseDir
      }
    }
    else {
      baseDir
    }
    val home = new File(System.getProperty("campfire.home", homeDir.getAbsolutePath))

    val realBaseDir = baseDir.getAbsoluteFile.getCanonicalFile
    val realHomeDir = home.getAbsoluteFile.getCanonicalFile

    new BaseHome(realBaseDir, realHomeDir)
  }

}

class BaseHome(var baseDir: File, var homeDir: File) {
  def initialize(args: StartArgs) {

  }
}
