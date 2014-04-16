package org.goldratio.xmpp.processor

import org.goldratio.xmpp.protocol.iq._
import org.goldratio.xmpp.protocol
import org.goldratio.xmpp.protocol.{Extension, Stanza, JID, Packet}
import org.goldratio.xmpp.protocol.extensions.auth.AuthenticationRequest
import org.goldratio.xmpp.network.{Transport, AuthenticationResult}
import org.goldratio.xmpp.protocol.extensions.bind
import org.goldratio.xmpp.protocol.extensions.session
import org.goldratio.xmpp.protocol.extensions.vcard
import org.goldratio.xmpp.protocol.extensions.roster
import org.goldratio.xmpp.protocol.extensions.ping
import org.goldratio.xmpp.protocol.extensions.register
import org.goldratio.xmpp.protocol.extensions.register.RegistrationRequest
import org.goldratio.xmpp.protocol.extensions.disco
import scala.collection._
import org.goldratio.xmpp.database.DbConnectionManager
import java.sql.{SQLException, ResultSet, PreparedStatement, Connection}
import org.goldratio.xmpp.user.UserAlreadyExistsException

/**
 * Created by GoldRatio on 4/12/14.
 */
class RosterProcessor(connectionManager: DbConnectionManager) extends Processor("") {

  val loadRosterSQL: String = "select xml from cam_roster where uid = ?"

  override def process(stanza: Stanza, transport: Transport): Unit = {

  }

  override def processorSet(set: protocol.iq.Set, transport: Transport): Unit = {

  }

  override def processorGet(get: protocol.iq.Get, transport: Transport): Unit = {
    val buffer = new StringBuffer( )
    buffer.append("<iq id='")
    buffer.append(get.id.get)
    buffer.append("' to='")
    buffer.append(transport.jid.toString)
    buffer.append("' type='result'>")
    var connection: Connection = null
    var stmt: PreparedStatement = null
    var rs: ResultSet = null
    try {
      connection = connectionManager.getConnection
      stmt = connection.prepareStatement(loadRosterSQL)
      stmt.setString(1, transport.jid.get.domain)
      rs = stmt.executeQuery
      while (rs.next()) {
       val xml = rs.getString(1)
       buffer.append(xml)
      }
    }
    catch {
      case e: SQLException => {
        e.printStackTrace
      }
    }
    finally {
      connectionManager.closeConnection(rs, stmt, connection)
    }
    buffer.append("</iq>")
    transport.send(buffer.toString)
  }
}

class IQProcessor(rosterProcessor: RosterProcessor) extends Processor("iq") {
  val domain = "mytask.com"

  override def process(iq: Stanza, transport: Transport) = {
    iq match {
      case get @ Get(_, _, _, Some(request:AuthenticationRequest)) => {
        get.result(Some(AuthenticationRequest("", "" , Some(""), None)))
      }
      case set @ protocol.iq.Set(_, _, _, Some(request:bind.BindRequest)) => {
        transport.setResource(request.resource)
        // TODO: add hooks for subclasses to control binding behavior
        transport.send(set.result(Some(bind.BindResult(transport.jid.get))).xml)
      }
      case set @ protocol.iq.Set(_, _, _, Some(request:session.Session)) => {
        //        state = 4
        transport.send(set.result(Some(session.Session())).xml)
      }
      case get @ protocol.iq.Get(_, _, _, Some(request:ping.Ping)) => {
        //        state = 4
        transport.send(get.result(Some(ping.Ping())).xml)
      }
      case get @ protocol.iq.Get(_, _, _, Some(request:vcard.VCard)) => {
        //        state = 4
        //transport.send(get.result(Some(vcard.VCard())))
      }
      case get @ protocol.iq.Get(_, _, _, Some(request:roster.RosterRequest)) => {
        //        state = 4
        //transport.send(get.result(Some(vcard.VCard())))
        rosterProcessor.processorGet(get, transport)
      }
      case get @ Get(_, _, _, Some(request:disco.InfoRequest)) => {
        handleDiscoInfoRequest(get, request, transport)
      }
      case get @ Get(_, _, _, Some(request:disco.ItemsRequest)) => {
        handleDiscoItemsRequest(get, request, transport)
      }
//      case set @ protocol.iq.Set(_, _, _, Some(request:AuthenticationRequest)) => {
//        // not sure if this is the right place to do the JId binding under 0078
//        transport.jid = JID(request.username, domain, request.resource)
//        transport.authenticate(new AuthenticationRequest(request.username, request.password.getOrElse(""))) match {
//          case AuthenticationResult.Success =>
//            transport.send(set.result())
//            this.delegate.get.onOnline(this.jid.get)
//          case AuthenticationResult.NotAuthorized =>
//            send(set.error(StanzaErrorCondition.NotAuthorized, Some("Invalid username or password")))
//          case AuthenticationResult.CredentialsExpired => send(set.error(StanzaErrorCondition.NotAuthorized, Some("Credential Expired")))
//          case AuthenticationResult.AccountDisabled => send(set.error(StanzaErrorCondition.NotAuthorized, Some("Account Disabled")))
//          case _ => send(set.error(StanzaErrorCondition.UndefinedCondition, Some("Unknown authentication error")))
//        }
//      }
      // xep-???
//      case set @ protocol.iq.Set(_, _, _, Some(request:bind.UnbindRequest)) => {
//        // TODO: add hooks for subclasses to control unbinding behavior
//        send(set.result(Some(session.Session())))
//      }
//      case get @ Get(_, _, _, Some(request:register.RegistrationRequest)) =>
//      {
//        // TODO: add hooks for subclasses to control registration behavior
//        send(get.result(Some(register.RegistrationRequest("", "" , "")) ))
//      }
//      case set @ protocol.iq.Set(_, _, _, Some(request:register.RegistrationRequest)) =>
//      {
//        // TODO: add hooks for subclasses to control registration behavior
//        this.delegate.get.register(new RegistrationRequest(request.username, request.password, request.email)) match
//        {
//          case RegistrationResult.Success => send(set.result())
//          case RegistrationResult.NotAcceptable => send(set.error(StanzaErrorCondition.NotAcceptable, Some("Request bot acceptable")))
//          case RegistrationResult.Conflict => send(set.error(StanzaErrorCondition.Conflict, Some("Username conflict")))
//          case RegistrationResult.NotImplemented => send(set.error(StanzaErrorCondition.NotImplemented, Some("Registration not supported")))
//          case RegistrationResult.Unknown => send(set.error(StanzaErrorCondition.UndefinedCondition, Some("Unknown registration error")))
//        }
//      }
      case _ =>

    }


  }

  private def handleDiscoInfoRequest(get:Get, request:disco.InfoRequest, transport: Transport) {

    get.to match {
      case Some(jid) if jid == transport.jid => {
        transport.send(get.result(Some(request.result(this.identities, this.features))).xml)
      }
      case Some(jid) => {
        getChildDiscoInfo(jid, request) match {
          case info: Option[Extension] =>
            transport.send(get.result(info).xml)
          case _ => // do nothing
        }
      }
      case _ => // do nothing
    }
  }

  private def handleDiscoItemsRequest(get:Get, request:disco.ItemsRequest, transport: Transport) {

    get.to match {
      case Some(jid) if jid == transport.jid => {
        getDiscoItems(request) match {
          case items: Option[Extension] =>
            transport.send(get.result(items).xml)
          case _ => // do nothing
        }
      }
      case Some(jid) => {
        getChildDiscoItems(jid, request) match {
          case items: Option[Extension] =>
            transport.send(get.result(items).xml)
          case _ => // do nothing
        }
      }
      case _ => // do nothing
    }

  }

  protected def getDiscoItems(request:disco.ItemsRequest):Option[disco.ItemsResult] = None

  protected def getChildDiscoItems(jid:JID, request:disco.ItemsRequest):Option[disco.ItemsResult] = None

  protected def getChildDiscoInfo(jid:JID, request:disco.InfoRequest):Option[disco.InfoResult] = None

  protected val identities:Seq[disco.Identity] = Nil

  protected val features:Seq[disco.Feature] = Nil

  override def processorSet(set: protocol.iq.Set, transport: Transport): Unit = {

  }

  override def processorGet(get: protocol.iq.Get, transport: Transport): Unit = {

  }
}
