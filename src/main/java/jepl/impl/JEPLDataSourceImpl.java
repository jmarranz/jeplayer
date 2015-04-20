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
package jepl.impl;

import jepl.impl.query.JEPLResultSetDAOListenerDefaultImpl;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import jepl.JEPLBoot;
import jepl.JEPLConnection;
import jepl.JEPLDAL;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLResultSetDAOListenerDefault;
import jepl.JEPLResultSetDAOBeanMapper;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLUpdateDAOBeanMapper;
import jepl.JEPLUpdateDAOListener;
import jepl.JEPLUpdateDAOListenerDefault;
import jepl.impl.query.JEPLUpdateDAOListenerDefaultImpl;

/**
 * A la hora de heredar entre los sistemas NonJTA y JTA hay que conseguir que
 * JEPLayer funcionen sin dependencias JTA (clases java.transaction)
 * pues mientras el hilo de ejecución no pase por las clases JTA internas
 * no se intentarán cargar dependencias JTA
 *
 * @author jmarranz
 */
public abstract class JEPLDataSourceImpl implements JEPLDataSource
{
    protected JEPLBootImpl boot;
    protected DataSource ds;
    protected ThreadLocal<JEPLConnectionImpl> connectionByThread = new ThreadLocal<JEPLConnectionImpl>();
    protected JEPLListenerListImpl listenerList = new JEPLListenerListImpl();
    protected JEPLUserDataMultiThreadImpl userData = new JEPLUserDataMultiThreadImpl();
    protected boolean preparedStatementCached = true;
    protected volatile boolean inUse = false; // La verdad es que el volatile sobra pues es para detectar errores en tiempo de desarollo
    protected volatile boolean isC3PO = false;
    
    public JEPLDataSourceImpl(JEPLBootImpl boot,DataSource ds)
    {
        this.boot = boot;
        this.ds = ds;
    }

    @Override
    public String[] getUserDataNames()
    {
        return userData.getUserDataNames();
    }

    @Override
    public boolean containsName(String name)
    {
        return userData.containsName(name);
    }

    @Override
    public Object getUserData(String name)
    {
        return userData.getUserData(name);
    }

    @Override
    public <T> T getUserData(String name, Class<T> returnType)
    {
        return userData.getUserData(name, returnType);
    }

    @Override
    public Object setUserData(String name, Object value)
    {
        return userData.setUserData(name, value);
    }

    @Override
    public Object removeUserData(String name)
    {
        return userData.removeUserData(name);
    }

    @Override
    public <T> T removeUserData(String name, Class<T> returnType)
    {
        return userData.removeUserData(name, returnType);
    }

    public boolean isInUse()
    {
        return inUse;
    }

    public boolean isC3PO()
    {
        return isC3PO;
    }
    
    public void setIsC3PO(boolean isC3PO)
    {
        this.isC3PO = isC3PO;
    }
    
    @Override
    public JEPLBoot getJEPLBoot()
    {
        return boot;
    }

    public JEPLBootImpl getJEPLBootImpl()
    {
        return boot;
    }

    @Override
    public DataSource getDataSource()
    {
        return ds;
    }

    @Override
    public boolean isPreparedStatementCached()
    {
        return preparedStatementCached;
    }

    @Override
    public void setPreparedStatementCached(boolean value)
    {
        checkIsInUse();
        this.preparedStatementCached = value;
    }   

    public JEPLListenerListImpl getJEPLListenerList()
    {
        return listenerList;
    }

    @Override
    public void addJEPLListener(JEPLListener listener)
    {
        if (listener instanceof JEPLResultSetDAOListener || listener instanceof JEPLUpdateDAOListener)
            throw new JEPLException("You cannot register a DAO listener in this level"); // Porque sólo se permite uno de cada tipo de listener y clases-modelo hay varias
        
        listenerList.addJEPLListener(listener);
    }

    public void removeJEPLListener(JEPLListener listener)
    {
        listenerList.removeJEPLListener(listener);
    }

    public JEPLConnection getCurrentJEPLConnection()
    {
        return getCurrentJEPLConnectionImpl(); // Puede ser null
    }

    public JEPLConnectionImpl getCurrentJEPLConnectionImpl() 
    {
        return connectionByThread.get(); // Puede ser null
    }

    protected void checkIsInUse()
    {
        if (inUse) throw new JEPLException("DataSource is already in use");
    }

    public JEPLConnectionImpl getJEPLConnectionFromPool() throws SQLException
    {
        Connection con = ds.getConnection();
        return createJEPLConnection(con); // Este constructor no dará nunca error
    }

    public void returnJEPLConnectionToPool(JEPLConnectionImpl jcon) throws SQLException
    {
        jcon.closePreparedStatements();

        Connection con = jcon.getConnection();
        con.close(); // Devolver al pool
    }

    @Override
    public <T> JEPLResultSetDAOListenerDefault<T> createJEPLResultSetDAOListenerDefault(Class<T> clasz,JEPLResultSetDAOBeanMapper<T> mapper)
    {
        try
        {
            return new JEPLResultSetDAOListenerDefaultImpl<T>(clasz,mapper);
        }
        catch(Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public <T> JEPLResultSetDAOListenerDefault<T> createJEPLResultSetDAOListenerDefault(Class<T> clasz)
    {
        return createJEPLResultSetDAOListenerDefault(clasz,null);
    }

    @Override
    public <T> JEPLUpdateDAOListenerDefault<T> createJEPLUpdateDAOListenerDefault(Class<T> clasz,JEPLUpdateDAOBeanMapper<T> mapper)
    {
        try
        {
            return new JEPLUpdateDAOListenerDefaultImpl<T>(clasz,mapper);
        }
        catch(Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public <T> JEPLUpdateDAOListenerDefault<T> createJEPLUpdateDAOListenerDefault(Class<T> clasz)
    {
        return createJEPLUpdateDAOListenerDefault(clasz,null);
    }    
    
    public <T> JEPLConnectionImpl pushJEPLTask(JEPLTaskExecContextInConnectionImpl<T> task) throws SQLException
    {
        this.inUse = true;
        listenerList.setInUse();
        getJEPLBootImpl().setInUse();

        JEPLConnectionImpl jcon = connectionByThread.get();
        if (jcon == null)
        {
            jcon = getJEPLConnectionFromPool(); // Si ocurre una excepción será aquí no devolviendo la conexión
            connectionByThread.set(jcon);
        }

        jcon.pushJEPLTaskExecContex(task);
        task.setJEPLConnection(jcon);
        return jcon;
    }

    public <T> void popJEPLTask(JEPLTaskExecContextInConnectionImpl<T> task) throws SQLException
    {
        task.setJEPLConnection(null);

        JEPLConnectionImpl jcon = connectionByThread.get(); // jcon NO puede ser nulo
        if (jcon.popJEPLTaskExecContex() != task) throw new JEPLException("INTERNAL ERROR");

        if (jcon.isEmptyOfJEPLTasks())
        {
            // La vida del JEPLConnectionImpl coincide con la vida del Connection fuera del pool
            connectionByThread.set(null);

            returnJEPLConnectionToPool(jcon); // Devolver al pool
        }
    }

    public JEPLDAL createJEPLDAL()
    {
        return new JEPLDALDefaultImpl(this);
    }

    public <T> JEPLDAO<T> createJEPLDAO(Class<T> type)
    {
        return new JEPLDAOImpl<T>(this);
    }

    public abstract <T> T execInternal(JEPLTaskOneExecWithConnectionImpl<T> task,JEPLDALImpl dal,JEPLListenerListImpl paramListener) throws Exception;

    public abstract JEPLConnectionImpl createJEPLConnection(Connection con) throws SQLException;
}
