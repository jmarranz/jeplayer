/*
   Copyright 2011 Jose Maria Arranz Santamaria

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package example.loadmanually;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author jmarranz
 */
public class SimpleDataSource implements DataSource 
{
    protected String url;
    protected String login;
    protected String password;
    protected final SimpleConnectionWrapper[] connList;
 
    public SimpleDataSource(String jdbcDriverClassName, String url, String login, String password,int numConn) 
    {    
    	this(createDriver(jdbcDriverClassName),url,login,password,numConn);
    }
    
    public SimpleDataSource(Driver driver, String url, String login, String password,int numConn) 
    {
        this.url = url;
        this.login = login;
        this.password = password;
        this.connList = new SimpleConnectionWrapper[numConn];
        try 
        {
            DriverManager.registerDriver(driver);
            for(int i = 0; i < numConn; i++)
            {
                Connection conn = DriverManager.getConnection(url,login, password);
                connList[i] = new SimpleConnectionWrapper(this,conn,i); 
            }
        }
        catch (SQLException ex) { throw new RuntimeException(ex); }  
        
    }
        
    private static Driver createDriver(String jdbcDriverClassName)
    {
    	try
    	{
	        Class<?> driverClass = Class.forName(jdbcDriverClassName);
	        Constructor<?> construc = driverClass.getConstructor(new Class<?>[0]);
	        return (Driver)construc.newInstance((Object[])null);
    	}
    	catch (Exception ex)
    	{
    		throw new RuntimeException(ex);
    	}        
    }
    
    public synchronized void destroy() throws SQLException 
    {
        for(int i = 0; i < connList.length; i++)
        {
            connList[i].getInternalConnection().close();
            connList[i] = null;
        }        
    }
    
    public synchronized Connection getConnection() throws SQLException 
    {
        for(int i = 0; i < connList.length; i++)
        {
            SimpleConnectionWrapper connWrap = connList[i];
            if (connWrap.isInUse()) continue;
            connWrap.holdConnection();
            return connWrap;
        }
        throw new SQLException("No more free connections");
    }

    public synchronized void releaseConnection(SimpleConnectionWrapper connWrap)
    {
        connWrap.releaseConnection();
    }
            
    public Connection getConnection(String username, String password) throws SQLException 
    {
        return DriverManager.getConnection(url,username, password); 
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
