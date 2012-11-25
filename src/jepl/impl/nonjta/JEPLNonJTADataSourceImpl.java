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

package jepl.impl.nonjta;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jepl.JEPLBootNonJTA;
import jepl.JEPLConnectionListener;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLNonJTADataSource;
import jepl.JEPLTask;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLDataSourceImpl;
import jepl.impl.JEPLListenerListImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionWrapperImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLNonJTADataSourceImpl extends JEPLDataSourceImpl implements JEPLNonJTADataSource
{
    protected boolean useJDBCAutoCommit = true; // No transacción por defecto   
    
    public JEPLNonJTADataSourceImpl(JEPLBootNonJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
    }

    @Override
    public JEPLBootNonJTA getJEPLBootNonJTA()
    {
        return (JEPLBootNonJTAImpl)boot;
    }

    @Override
    public void returnJEPLConnectionToPool(JEPLConnectionImpl jcon) throws SQLException
    {
        Connection con = jcon.getConnection();
        if (!con.getAutoCommit())
            con.setAutoCommit(true); // Incluso en el caso de transacción finalizada es conveniente

        super.returnJEPLConnectionToPool(jcon);
    }

    public boolean isDefaultAutoCommit()
    {
        return useJDBCAutoCommit;
    }

    public void setDefaultAutoCommit(boolean value)
    {
        checkIsInUse();
        this.useJDBCAutoCommit = value;
    }

    @Override
    public JEPLConnectionImpl createJEPLConnection(Connection con) throws SQLException
    {
        return new JEPLNonJTAConnectionDefaultImpl(this,con); // Este constructor no dará nunca error
    }
    
    
    @Override
    public <T> T exec(JEPLTask<T> task)
    {
        try
        {
            return execInternal(task,null);
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public <T> T exec(JEPLTask<T> task,JEPLListener listener)
    {
        try
        {
            return execInternal(task,JEPLListenerListImpl.getJEPLListenerList(listener));
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public <T> T exec(JEPLTask<T> task,boolean autoCommit)
    {
        try
        {
            JEPLConnectionListener<?> paramListener = JEPLNonJTAConnectionImpl.createJEPLConnectionListener(autoCommit);
            return execInternal(task,JEPLListenerListImpl.getJEPLListenerList(paramListener));
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public <T> T execInternal(JEPLTask<T> task,JEPLListenerListImpl paramListener) throws Exception
    {
        JEPLTaskOneExecWithConnectionWrapperImpl<T> taskWrap = new JEPLTaskOneExecWithConnectionWrapperImpl<T>(task);
        return execInternal(taskWrap,null,paramListener);
    }

    @Override
    public <T> T execInternal(JEPLTaskOneExecWithConnectionImpl<T> task,JEPLDALImpl dal,JEPLListenerListImpl paramListener) throws Exception
    {
        JEPLTaskExecContextInConnectionNonJTAImpl<T> taskCtx = new JEPLTaskExecContextInConnectionNonJTAImpl<T>(task);
        JEPLNonJTAConnectionImpl conWrap = (JEPLNonJTAConnectionImpl)pushJEPLTask(taskCtx);
        try
        {
            return conWrap.execTask(taskCtx,dal,paramListener);
        }
        finally
        {
            try
            {
                popJEPLTask(taskCtx);
            }
            catch (SQLException ex)
            {
                throw new JEPLException(ex);
            }
        }
    }
}
