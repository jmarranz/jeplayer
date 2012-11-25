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

package jepl.impl.jta;

import jepl.impl.jta.dsmgr.JEPLJTAMultipleDataSourceImpl;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLException;
import jepl.JEPLJTADataSource;
import jepl.JEPLListener;
import jepl.JEPLTask;
import jepl.JEPLTransactionPropagation;
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
public abstract class JEPLJTADataSourceImpl extends JEPLDataSourceImpl implements JEPLJTADataSource
{
    protected JEPLTransactionPropagation defaultTransactionPropagation;
    protected ThreadLocal<JEPLConnectionImpl> connByThreadOfDataSourceMgr = new ThreadLocal<JEPLConnectionImpl>();
    
    public JEPLJTADataSourceImpl(JEPLBootJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
        this.defaultTransactionPropagation = boot.getJEPLJTAMultipleDataSourceImpl().getDefaultJEPLTransactionPropagation();
    }

    public JEPLJTAMultipleDataSourceImpl getJEPLJTAMultipleDataSourceImpl()
    {
        return getJEPLBootJTAImpl().getJEPLJTAMultipleDataSourceImpl();
    }

    public JEPLBootJTA getJEPLBootJTA()
    {
        return (JEPLBootJTAImpl)boot;
    }
    
    public JEPLBootJTAImpl getJEPLBootJTAImpl()
    {
        return (JEPLBootJTAImpl)boot;
    }

    public UserTransaction getUserTransaction()
    {
        return getJEPLBootJTAImpl().getUserTransaction();
    }

    public TransactionManager getTransactionManager()
    {
        return getJEPLBootJTAImpl().getTransactionManager();
    }
    
    public JEPLTransactionPropagation getDefaultJEPLTransactionPropagation()
    {
        return defaultTransactionPropagation;
    }

    public void setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation defaultTransactionPropagation)
    {
        checkIsInUse();
        this.defaultTransactionPropagation = defaultTransactionPropagation;
    }

    public JEPLJTAConnectionImpl getCurrentJEPLJTAConnectionImpl()
    {
        return (JEPLJTAConnectionImpl)getCurrentJEPLConnectionImpl(); // Puede ser null
    }

    public JEPLTransactionPropagation getCurrentJEPLTransactionPropagation()
    {
        JEPLJTAConnectionImpl jcon = getCurrentJEPLJTAConnectionImpl();
        if (jcon == null) return null;
        JEPLTaskExecContextInConnectionJTAImpl taskCtx = (JEPLTaskExecContextInConnectionJTAImpl)jcon.getCurrentJEPLTaskContext();
        if (taskCtx == null) return null; // Yo creo que nunca es null pero por si acaso
        return taskCtx.getJEPLTransactionPropagation();
    }

    @Override
    public JEPLConnectionImpl createJEPLConnection(Connection con)
    {
        return new JEPLJTAConnectionDefaultImpl(this,con); // Este constructor no dará nunca error
    }
    
    @Override
    public JEPLConnectionImpl getCurrentJEPLConnectionImpl()
    {
        if (!getJEPLJTAMultipleDataSourceImpl().hasCurrentJEPLTask())
            return super.getCurrentJEPLConnectionImpl();
        else
            return connByThreadOfDataSourceMgr.get();
    }

    @Override
    public JEPLConnectionImpl getJEPLConnectionFromPool() throws SQLException
    {
        if (!getJEPLJTAMultipleDataSourceImpl().hasCurrentJEPLTask())
            return getJEPLConnectionFromPoolEffective();
        else
            return connByThreadOfDataSourceMgr.get();
    }


    @Override
    public void returnJEPLConnectionToPool(JEPLConnectionImpl jcon) throws SQLException
    {
        if (!getJEPLJTAMultipleDataSourceImpl().hasCurrentJEPLTask())
            returnJEPLConnectionToPoolEffective(jcon);
        else
        {
            //int i = 1;
        }
        // En caso contrario no hacer nada, pues se cierra cuando se cierre la última task del task manager
    }

    public void getJEPLConnectionFromPoolByDataSourceMgr() throws SQLException
    {
        JEPLConnectionImpl jcon = getJEPLConnectionFromPoolEffective();
        connByThreadOfDataSourceMgr.set(jcon);
    }

    public void returnJEPLConnectionToPoolByDataSourceMgr() throws SQLException
    {
        JEPLConnectionImpl jcon = connByThreadOfDataSourceMgr.get();
        connByThreadOfDataSourceMgr.set(null);
        returnJEPLConnectionToPoolEffective(jcon);
    }

    protected JEPLConnectionImpl getJEPLConnectionFromPoolEffective() throws SQLException
    {
        return super.getJEPLConnectionFromPool();
    }

    protected void returnJEPLConnectionToPoolEffective(JEPLConnectionImpl jcon) throws SQLException
    {
        //jcon.getConnection().setAutoCommit(false);

        super.returnJEPLConnectionToPool(jcon);
    }


    @Override
    public <T> T exec(JEPLTask<T> task)
    {
        return exec(task,null,null);
    }

    @Override
    public <T> T exec(JEPLTask<T> task,JEPLListener listener)
    {
        return exec(task,listener,null);
    }

    @Override
    public <T> T exec(JEPLTask<T> task,JEPLTransactionPropagation txnProp)
    {
        return exec(task,null,txnProp);
    }

    @Override
    public <T> T exec(JEPLTask<T> task,JEPLListener paramListener,JEPLTransactionPropagation txnProp)
    {
        // El valor del JEPLTransactionPropagation no puede ser determinado por el posible
        // JEPLConnectionListener pasado por el usuario, pues la propagación está claramente
        // asociada al ámbito del método persistente es decir la task por lo que no puede/debe
        // ser cambiada en cualquier momento o fase.

        try
        {
            JEPLTaskOneExecWithConnectionWrapperImpl<T> taskWrap = new JEPLTaskOneExecWithConnectionWrapperImpl<T>(task);
            return execInternal(taskWrap,null,JEPLListenerListImpl.getJEPLListenerList(paramListener),txnProp);
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
    public <T> T execInternal(JEPLTaskOneExecWithConnectionImpl<T> task,JEPLDALImpl dal,JEPLListenerListImpl paramListener) throws Exception
    {
        return execInternal(task,dal,paramListener,null);
    }

    public <T> T execInternal(JEPLTaskOneExecWithConnectionImpl<T> task,final JEPLDALImpl dal,final JEPLListenerListImpl paramListener,JEPLTransactionPropagation txnPropParam) throws Exception
    {
        JEPLTask<T> originalTask = task.getInnerJEPLTask();
        final JEPLTransactionPropagation txnProp = TransactionJTAExecutor.getJEPLTransactionPropagation(txnPropParam, originalTask, getDefaultJEPLTransactionPropagation()); // Este es finalmente el que se usa (si se usa)
        final JEPLTaskExecContextInConnectionJTAImpl<T> taskCtx = new JEPLTaskExecContextInConnectionJTAImpl<T>(task,txnProp);

        //HACER JTA bueno y Fake, no olvidar de quitar la transacción dentro de JEPLJTAConnectionImpl
        //eliminando  finalmente TransactionJTAUtil
                

        UserTransaction userTxn = getUserTransaction();
        TransactionManager txnMgr = getTransactionManager();
        if (userTxn instanceof UserTransactionJDBC)
        {
            // Fake UserTransaction (pure JDBC based)
            // Necesitamos obtener antes la conexión antes de iniciar la transacción
            final JEPLJTAConnectionImpl conWrap = (JEPLJTAConnectionImpl)pushJEPLTask(taskCtx);
            try
            {
                TransactionJTAExecutor<T> jtaTxn = new TransactionJTAExecutor<T>()
                {
                    @Override
                    public  T onExecute() throws Exception
                    {
                        return conWrap.execTask(taskCtx,dal,paramListener,txnProp);
                    }
                };
                return jtaTxn.execute(txnProp,userTxn,txnMgr);
            }
            finally
            {
                popJEPLTask(taskCtx);
            }
        }
        else
        {
            // Iniciamos la transacción antes de obtener la conexión
            // el UserTransaction detectará las conexiones (si son XA) que se abran
            // y las añadirá a la transacción, en JOTM y Atomikos podría ser en el orden
            // inverso pues los DataSource XA están ya "preregistrados" pero esto no estándar
            TransactionJTAExecutor<T> jtaTxn = new TransactionJTAExecutor<T>()
            {
                @Override
                public  T onExecute() throws Exception
                {
                    JEPLJTAConnectionImpl conWrap = (JEPLJTAConnectionImpl)pushJEPLTask(taskCtx);
                    try
                    {
                        return conWrap.execTask(taskCtx,dal,paramListener,txnProp);
                    }
                    finally
                    {
                        popJEPLTask(taskCtx);
                    }
                }
            };
            return jtaTxn.execute(txnProp, userTxn,txnMgr);
        }
    }
}
