package org.goldratio.xmpp.network

/**
 * Created by GoldRatio on 4/9/14.
 */
trait Disconnectable {

  def onDisconnect(client: AbstractConnection)

}
