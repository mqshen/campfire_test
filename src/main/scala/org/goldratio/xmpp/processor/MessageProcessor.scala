package org.goldratio.xmpp.processor

import org.goldratio.xmpp.protocol.{Packet, Stanza}
import org.goldratio.xmpp.network.Transport
import org.goldratio.xmpp.protocol.iq.{Set, Get}
import org.goldratio.xmpp.protocol.message.Message
import org.goldratio.xmpp.database.DbConnectionManager
import java.sql.{SQLException, ResultSet, PreparedStatement, Connection}

/**
 * Created by GoldRatio on 4/14/14.
 */
class MessageProcessor(connectionManager: DbConnectionManager) extends Processor("message") {

  val createMessageSQL: String = "insert into cam_message(sender, receiver, message) values(?, ?, ?)"

  override def processorSet(set: Set, transport: Transport): Unit = {

  }

  override def processorGet(get: Get, transport: Transport): Unit = {

  }

  override def process(message: Stanza, transport: Transport): Unit = {
    message match {
      case msg: Message => {
        val forward = msg.dispatch(transport.jid)
       transport.handler(forward)
        var connection: Connection = null
        var stmt: PreparedStatement = null
        try {
          connection = connectionManager.getConnection
          stmt = connection.prepareStatement(createMessageSQL)
          stmt.setString(1, forward.from.get.domain)
          stmt.setString(2, forward.to.get.node)
          stmt.setString(3, forward.toString)
          stmt.executeUpdate()
        }
        catch {
          case e: SQLException => {
            e.printStackTrace
          }
        }
        finally {
          connectionManager.closeConnection(stmt, connection)
        }

      }
    }

  }

}
