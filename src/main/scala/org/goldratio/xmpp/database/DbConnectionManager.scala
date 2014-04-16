package org.goldratio.xmpp.database

import java.sql.{Statement, ResultSet, SQLException, Connection}

/**
 * Created by GoldRatio on 4/9/14.
 */
object DatabaseType extends Enumeration {
  type DataBaseType = Value
  val oracle, postgresql, mysql, hsqldb, db2, sqlserver, interbase, unknown = Value
}

class DbConnectionManager {
  var connectionProvider: ConnectionProvider = null
  var transactionsSupported: Boolean = false
  var streamTextRequired: Boolean = false
  var maxRowsSupported = false
  var fetchSizeSupported = false
  var subqueriesSupported = false
  var scrollResultsSupported = false
  var batchUpdatesSupported = false
  var pstmt_fetchSizeSupported = true

  var databaseType = DatabaseType.unknown

  object providerLock

  def getConnection(time: Int): Option[Connection] = {
    val con = connectionProvider.getConnection()
    val retryWait = 250 // milliseconds
    if(con != null) {
      Some(con)
    }
    else {
      Thread.sleep(retryWait)
      if(time < 0)
        None
      else
        getConnection(time - 1)
    }
  }
  /**
   * Returns a database connection from the currently active connection
   * provider. An exception will be thrown if no connection was found.
   * (auto commit is set to true).
   *
   * @return a connection.
   * @throws SQLException if a SQL exception occurs or no connection was found.
   */
  def getConnection: Connection = {
    if (connectionProvider == null) {
      throw new SQLException( "connection provide not found")
    }
    val conn = getConnection(4)
    if(conn.isDefined)
      conn.get
    else
      throw new SQLException( "connection can not get")
  }

  def closeStatement(stmt: Statement) {
    if (stmt != null) {
      try {
        stmt.close
      }
      catch {
        case e: Exception => {
        }
      }
    }
  }

  def closeResultSet(set: ResultSet) = {
    if (set != null) {
      try {
        set.close
      }
      catch {
        case e: Exception => {
        }
      }
    }
  }

  def closeStatement(rs: ResultSet, stmt: Statement) {
    closeResultSet(rs)
    closeStatement(stmt)
  }

  def closeConnection(stmt: Statement, con: Connection) {
    closeStatement(stmt)
    closeConnection(con)
  }

  def closeConnection(rs: ResultSet, stmt: Statement, con: Connection) {
    closeResultSet(rs)
    closeStatement(stmt)
    closeConnection(con)
  }

  def closeConnection(con: Connection) {
    if (con != null) {
      try {
        con.close
      }
      catch {
        case e: Exception => {
        }
      }
    }
  }



  def setConnectionProvider(provider: ConnectionProvider ) {
    providerLock synchronized {
      if (connectionProvider != null) {
        connectionProvider.destroy();
        connectionProvider = null;
      }
      connectionProvider = provider;
      // Now, get a connection to determine meta data.
      var con:Connection = null
      try {
        con = connectionProvider.getConnection()
        setMetaData(con)
      }
      finally {
        closeConnection(con);
      }
    }
  }

  def  setMetaData(con: Connection ) {
    val metaData = con.getMetaData()
    // Supports transactions?
    transactionsSupported = metaData.supportsTransactions()
    // Supports subqueries?
    subqueriesSupported = metaData.supportsCorrelatedSubqueries()
    // Supports scroll insensitive result sets? Try/catch block is a
    // workaround for DB2 JDBC driver, which throws an exception on
    // the method call.
    try {
      scrollResultsSupported = metaData.supportsResultSetType(
        ResultSet.TYPE_SCROLL_INSENSITIVE);
    }
    catch  {
      case e:Exception =>
        scrollResultsSupported = false;
    }
    // Supports batch updates
    batchUpdatesSupported = metaData.supportsBatchUpdates();

    // Set defaults for other meta properties
    streamTextRequired = false;
    maxRowsSupported = true;
    fetchSizeSupported = true;

    // Get the database name so that we can perform meta data settings.
    val dbName = metaData.getDatabaseProductName().toLowerCase();
    val driverName = metaData.getDriverName().toLowerCase();

    // Oracle properties.
    if (dbName.indexOf("oracle") != -1) {
      databaseType = DatabaseType.oracle;
      streamTextRequired = true;
      scrollResultsSupported = false; /* TODO comment and test this, it should be supported since 10g */
      // The i-net AUGURO JDBC driver
      if (driverName.indexOf("auguro") != -1) {
        streamTextRequired = false;
        fetchSizeSupported = true;
        maxRowsSupported = false;
      }
    }
    // Postgres properties
    else if (dbName.indexOf("postgres") != -1) {
      databaseType = DatabaseType.postgresql;
      // Postgres blows, so disable scrolling result sets.
      scrollResultsSupported = false;
      fetchSizeSupported = false;
    }
    // Interbase properties
    else if (dbName.indexOf("interbase") != -1) {
      databaseType = DatabaseType.interbase;
      fetchSizeSupported = false;
      maxRowsSupported = false;
    }
    // SQLServer
    else if (dbName.indexOf("sql server") != -1) {
      databaseType = DatabaseType.sqlserver;
      // JDBC driver i-net UNA properties
      if (driverName.indexOf("una") != -1) {
        fetchSizeSupported = true;
        maxRowsSupported = false;
      }
    }
    // MySQL properties
    else if (dbName.indexOf("mysql") != -1) {
      databaseType = DatabaseType.mysql;
      transactionsSupported = false; /* TODO comment and test this, it should be supported since 5.0 */
    }
    // HSQL properties
    else if (dbName.indexOf("hsql") != -1) {
      databaseType = DatabaseType.hsqldb;
      // scrollResultsSupported = false; /* comment and test this, it should be supported since 1.7.2 */
    }
    // DB2 properties.
    else if (dbName.indexOf("db2") != 1) {
      databaseType = DatabaseType.db2;
    }
  }


}
