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

package jepl.impl.jta.dsmgr;

import java.sql.SQLException;
import java.util.LinkedList;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLException;
import jepl.JEPLJTAMultipleDataSource;
import jepl.JEPLTask;
import jepl.impl.JEPLTaskOneExecutionWrapperImpl;
import jepl.JEPLTransactionPropagation;
import jepl.impl.jta.JEPLBootJTAImpl;
import jepl.impl.jta.JEPLJTADataSourceImpl;
import jepl.impl.jta.TransactionJTAExecutor;
import jepl.impl.jta.UserTransactionJDBC;

/**
 *
 * @author jmarranz
 */
public class JEPLJTAMultipleDataSourceImpl implements JEPLJTAMultipleDataSource
{
    protected JEPLBootJTAImpl root;
    protected JEPLTransactionPropagation defaultTransactionPropagation = JEPLTransactionPropagation.REQUIRED;
    protected final LinkedList<JEPLJTADataSourceImpl> dataSourceList = new LinkedList<JEPLJTADataSourceImpl>(); // No hace falta que la sincronicemos porque será sólo lectura una vez que se hayan registrado los datasource (suponemos con el mismo hilo)
    protected ThreadLocal<JEPLTaskListSameThreadImpl> taskListByThread = new ThreadLocal<JEPLTaskListSameThreadImpl>();
    protected volatile boolean inUse = false; // La verdad es que el volatile sobra pues es para detectar errores en tiempo de desarollo

    public JEPLJTAMultipleDataSourceImpl(JEPLBootJTAImpl root)
    {
        this.root = root;
    }

    public void addJEPLJTADataSource(JEPLJTADataSourceImpl jds)
    {
        checkIsInUse(); // Si está ya en uso el dataSourceList debe considerarse "congelado" como sólo lectura
        synchronized(dataSourceList)
        {
            dataSourceList.add(jds);
        }
        // El synchronized es para permitir que el proceso de registro de los JEPLJTADataSourceImpl pueda
        // ser en varios hilos, así dataSourceList está "parcialmente sincronizada", sólo en tiempo de carga de los DataSource
        // luego será sólo lectura y no necesitará sincronización.
    }

    public LinkedList<JEPLJTADataSourceImpl> getJEPLJTADataSourceList()
    {
        return dataSourceList;
    }

    public JEPLBootJTA getJEPLBootJTA()
    {
        return root;
    }

    public UserTransaction getUserTransaction()
    {
        return root.getUserTransaction();
    }

    public TransactionManager getTransactionManager()
    {
        return root.getTransactionManager();
    }

    public JEPLTransactionPropagation getDefaultJEPLTransactionPropagation()
    {
        return defaultTransactionPropagation;
    }

    public void setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation defaultTransactionPropagation)
    {
        checkNoDataSource();
        this.defaultTransactionPropagation = defaultTransactionPropagation;
    }

    protected void checkNoDataSource()
    {
        if (!dataSourceList.isEmpty())
            throw new JEPLException("Some DataSource has been registered");
    }

    protected void checkIsInUse()
    {
        if (inUse)
            throw new JEPLException("You cannot register more DataSource because this manager is already in use");
    }
    
    public boolean isInUse()
    {
        return inUse;
    }

    public JEPLTransactionPropagation getCurrentJEPLTransactionPropagation()
    {
        JEPLTaskExecContextInJTADataSourceMgrImpl<?> taskCtx = getCurrentJEPLTask();
        if (taskCtx == null) return null; 
        return taskCtx.getJEPLTransactionPropagation();
    }

    public JEPLTaskExecContextInJTADataSourceMgrImpl<?> getCurrentJEPLTask()
    {
        JEPLTaskListSameThreadImpl taskList = taskListByThread.get();
        if (taskList == null) return null; 
        return taskList.getCurrentJEPLTaskContext();
    }

    public boolean hasCurrentJEPLTask()
    {
        JEPLTaskExecContextInJTADataSourceMgrImpl<?> taskCtx = getCurrentJEPLTask();
        return taskCtx != null;
    }

    public <T> void pushJEPLTask(JEPLTaskExecContextInJTADataSourceMgrImpl<T> task) throws SQLException
    {
        this.inUse = true;

        JEPLTaskListSameThreadImpl taskList = taskListByThread.get();
        if (taskList == null)
        {
            taskList = new JEPLTaskListSameThreadImpl();
            taskListByThread.set(taskList);

            for(JEPLJTADataSourceImpl jds : dataSourceList)
            {
                jds.getJEPLConnectionFromPoolByDataSourceMgr();
            }
        }
        taskList.pushJEPLTaskExecContex(task);
    }

    public void popJEPLTask(JEPLTaskExecContextInJTADataSourceMgrImpl task) throws SQLException
    {
        JEPLTaskListSameThreadImpl taskList = taskListByThread.get();  // taskList NO puede ser nulo
        if (taskList.popJEPLTaskExecContex() != task)
            throw new JEPLException("INTERNAL ERROR");

        if (taskList.isEmptyOfJEPLTasks())
        {
            taskListByThread.set(null);

            for(JEPLJTADataSourceImpl jds : dataSourceList)
            {
                jds.returnJEPLConnectionToPoolByDataSourceMgr();
            }
        }
    }


    public <T> T exec(JEPLTask<T> task)
    {
        try
        {
            JEPLTaskOneExecutionWrapperImpl<T> taskWrap = new JEPLTaskOneExecutionWrapperImpl<T>(task);
            return execInternal(taskWrap,null);
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

    public <T> T exec(JEPLTask<T> task, JEPLTransactionPropagation txnProp)
    {
        try
        {
            JEPLTaskOneExecutionWrapperImpl<T> taskWrap = new JEPLTaskOneExecutionWrapperImpl<T>(task);
            return execInternal(taskWrap,txnProp);
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

    public <T> T execInternal(final JEPLTaskOneExecutionWrapperImpl<T> task,JEPLTransactionPropagation txnPropParam) throws Exception
    {
        JEPLTask<T> originalTask = task.getInnerJEPLTask();
        final JEPLTransactionPropagation txnProp = TransactionJTAExecutor.getJEPLTransactionPropagation(txnPropParam, originalTask, getDefaultJEPLTransactionPropagation());  // Este es finalmente el que se usa
        final JEPLTaskExecContextInJTADataSourceMgrImpl<T> taskCtx = new JEPLTaskExecContextInJTADataSourceMgrImpl<T>(task,txnProp);

        UserTransaction userTxn = getUserTransaction();
        TransactionManager txnMgr = getTransactionManager();
        if (userTxn instanceof UserTransactionJDBC)
        {
            // Fake UserTransaction (pure JDBC based)
            // Necesitamos obtener antes las conexiones antes de iniciar la transacción
            
            pushJEPLTask(taskCtx);
            try
            {
                TransactionJTAExecutor<T> jtaTxn = new TransactionJTAExecutor<T>()
                {
                    @Override
                    public  T onExecute() throws Exception
                    {
                        return task.exec();
                    }
                };
                return jtaTxn.execute(txnProp, userTxn,txnMgr);
            }
            finally
            {
                popJEPLTask(taskCtx);
            }
        }
        else
        {
            // Iniciamos la transacción antes de obtener las conexiones
            // el UserTransaction detectará las conexiones (si son XA) que se abran
            // y las añadirá a la transacción, en JOTM y Atomikos podría ser en el orden
            // inverso pues los DataSource XA están ya "preregistrados" pero esto no estándar
            TransactionJTAExecutor<T> jtaTxn = new TransactionJTAExecutor<T>()
            {
                @Override
                public  T onExecute() throws Exception
                {
                    pushJEPLTask(taskCtx);
                    try
                    {
                        return task.exec();
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
