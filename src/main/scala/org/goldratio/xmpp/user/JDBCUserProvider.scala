package org.goldratio.xmpp.user

import java.sql.{SQLException, ResultSet, PreparedStatement, Connection}
import org.goldratio.xmpp.database.DbConnectionManager

/**
 * Created by GoldRatio on 4/9/14.
 */
class JDBCUserProvider(connectionManager: DbConnectionManager ) extends UserProvider {
  val loadUserSQL: String = "select name, email from user where name = ?"
  val createUserSQL: String = "insert into user(name, email, password) values(?, ?, ?)"

  def loadUser(username: String): User = {
    return null
  }

  def createUser(username: String, password: String, name: String, email: String): User = {
    var connection: Connection = null
    var pstmt: PreparedStatement = null
    var rs: ResultSet = null
    var createStmt: PreparedStatement = null
    try {
      connection = connectionManager.getConnection
      pstmt = connection.prepareStatement(loadUserSQL)
      pstmt.setString(1, username)
      rs = pstmt.executeQuery
      if (!rs.next) {
        createStmt = connection.prepareStatement(createUserSQL)
        createStmt.setString(1, username)
        createStmt.setString(2, email)
        createStmt.setString(3, password)
        createStmt.executeUpdate
      }
      else {
        throw new UserAlreadyExistsException
      }
    }
    catch {
      case e: SQLException => {
        e.printStackTrace
      }
    }
    finally {
      if (createStmt != null) {
        connectionManager.closeStatement(createStmt)
      }
      connectionManager.closeConnection(rs, pstmt, connection)
    }
    return null
  }




  /**
   * Returns the number of users in the system.
   *
   * @return the total number of users.
   */
  override def getUserCount: Int = {
    2
  }

  /**
   * Delets a user. This method should throw an
   * UnsupportedOperationException if this operation is not
   * supported by the backend user store.
   *
   * @param username the username to delete.
   */
  override def deleteUser(username: String): Unit = {

  }
}
