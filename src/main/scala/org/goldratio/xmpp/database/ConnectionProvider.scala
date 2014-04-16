package org.goldratio.xmpp.database

import java.sql.Connection

/**
 * Created by GoldRatio on 4/10/14.
 */
trait ConnectionProvider {

  def isPooled():Boolean

  def getConnection():Connection

  def restart()

  def destroy()

}
