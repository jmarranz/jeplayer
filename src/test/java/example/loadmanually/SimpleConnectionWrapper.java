/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.loadmanually;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 *
 * @author jmarranz
 */
public class SimpleConnectionWrapper implements Connection
{
    protected SimpleDataSource dataSource;
    protected Connection conn;
    protected int index;    
    protected boolean inuse = false;

    
    public SimpleConnectionWrapper(SimpleDataSource dataSource,Connection conn,int index)
    {
        this.dataSource = dataSource;
        this.conn = conn;
        this.index = index;
    }
    
    public void holdConnection()
    {
        this.inuse = true;
    }
    
    public void releaseConnection()
    {
        if (!inuse) return; // Ya liberada
        this.inuse = false;
    }
    
    public boolean isInUse()
    {
        return inuse;
    }
    
    public Connection getInternalConnection()
    {
        return conn;
    }
    
    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    public void close() throws SQLException {
        dataSource.releaseConnection(this);
    }

    public boolean isClosed() throws SQLException {        
        if (isInUse()) return conn.isClosed();
        else return true;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.createStatement(resultSetType,resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareStatement(sql,resultSetType,resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql,resultSetType,resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        conn.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        conn.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        return conn.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.createStatement(resultSetType,resultSetConcurrency,resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareCall(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return conn.prepareStatement(sql,autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return conn.prepareStatement(sql,columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql,columnNames);
    }

    public Clob createClob() throws SQLException {
        return conn.createClob();
    }

    public Blob createBlob() throws SQLException {
        return conn.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return conn.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return conn.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return conn.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        conn.setClientInfo(name,value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        conn.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return conn.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return conn.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return conn.createArrayOf(typeName,elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return conn.createStruct(typeName,attributes);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return conn.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return conn.isWrapperFor(iface);
    }

    public void setSchema(String schema) throws SQLException
    {
        conn.setSchema(schema);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getSchema() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void abort(Executor executor) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getNetworkTimeout() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
