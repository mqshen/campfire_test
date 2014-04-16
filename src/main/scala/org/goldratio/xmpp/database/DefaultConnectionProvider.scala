package org.goldratio.xmpp.database

import java.sql.{SQLException, DriverManager, Connection}

/**
 * Created by GoldRatio on 4/10/14.
 */
class DefaultConnectionProvider(driver: String,
                                 url: String,
                                 username: String,
                                 password: String) extends ConnectionProvider {
  override def destroy(): Unit = {

  }

  override def restart(): Unit = {

  }

  override def getConnection(): Connection = {
    try {
      Class.forName(this.driver);
      DriverManager.getConnection(url, username, password);
    }
    catch {
      case e: ClassNotFoundException =>
        throw new SQLException("DbConnectionProvider: Unable to find driver: "+e)
    }

  }

  override def isPooled(): Boolean = {
    return false
  }
}
