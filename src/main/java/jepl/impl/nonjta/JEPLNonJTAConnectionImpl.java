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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import jepl.JEPLConnection;
import jepl.JEPLConnectionListener;
import jepl.JEPLException;
import jepl.JEPLTask;
import jepl.JEPLTransactionalNonJTA;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLListenerListImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionWrapperImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLNonJTAConnectionImpl extends JEPLConnectionImpl
{
    public JEPLNonJTAConnectionImpl(JEPLNonJTADataSourceImpl ds,Connection con)
    {
        super(ds,con);
    }

    public JEPLNonJTADataSourceImpl getJEPLNonJTADataSource()
    {
        return (JEPLNonJTADataSourceImpl)ds;
    }

    public static <T> JEPLConnectionListener<?> createJEPLConnectionListener(final boolean autoCommit)
    {
        JEPLConnectionListener<T> listener = new JEPLConnectionListener<T>()
        {
            public void setupJEPLConnection(JEPLConnection con, JEPLTask<T> task) throws Exception
            {
                ((JEPLNonJTAConnectionImpl)con).configureAutoCommit(autoCommit);
            }
        };
        return listener;
    }

    public void configureAutoCommit(boolean autoCommit) throws SQLException
    {
        Connection con = getConnection();
        if (con.getAutoCommit() != autoCommit)
            con.setAutoCommit(autoCommit);
    }

    public <T> T execTask(JEPLTaskExecContextInConnectionNonJTAImpl<T> taskCtx,JEPLDALImpl dal,JEPLListenerListImpl paramListener) throws Exception
    {
        JEPLConnectionListener<T> listener = getJEPLConnectionListener(dal,paramListener);

        // Si el listener es del usuario el usuario si quiere puede demarcar la transacción
        // si la demarca ha de llamar a exec() del task.

        JEPLTaskOneExecWithConnectionImpl<T> task = taskCtx.getJEPLTaskWithJEPLConnection();
        this.currentTxn = new JEPLCurrentTransactionNonJTAImpl(this);
        if (listener != null) listener.setupJEPLConnection(this, task);

        if (task.isExecuted())
        {
            // En este caso el programador es el responsable del control de la transacción
            // (commit y rollback) si no lo ha hecho es problema suyo
            return task.getResult();
        }
        else
        {
            if (currentTxn.isUsed())
                throw new JEPLException("You have used the JEPLTransaction but the next task was not executed");

            if (listener == null)
            {
                // El usuario ha tenido la oportunidad de definir a través de un JEPLConnectionListener
                // el autoCommit y no la ha usado por lo que definimos el autoCommit por defecto
                // Esta configuración en teoría la podríamos ejecutar antes de ejecutar el JEPLConnectionListener
                // e incondicionalmente pero tiene el problema de que un setDefaultAutoCommit(true) hace un commit()
                // implícito, en el caso de task anidados podría hacer un commit indeseado si estuviera a false
                // cuando resulta que luego a través de parámetro autoCommit o JEPLConnectionListener queríamos mantener
                // el modo false para continuar la transacción creada en el task anidado padre

                boolean autoCommit;
                
                // Vemos si hay anotación en la task

                JEPLTask<T> originalTask = JEPLTaskOneExecWithConnectionWrapperImpl.getInnerJEPLTask(task);
                Method method = originalTask.getClass().getMethod("exec",(Class[])null);
                JEPLTransactionalNonJTA txnDec = method.getAnnotation(JEPLTransactionalNonJTA.class);
                if (txnDec != null)
                    autoCommit = txnDec.autoCommit();
                else // No hay anotación, el valor por defecto
                    autoCommit = getJEPLNonJTADataSource().isDefaultAutoCommit();

                configureAutoCommit(autoCommit);
            }

            return executeTask(task);
        }
    }

    public <T> T executeTask(JEPLTask<T> task) throws Exception
    {
        Connection con = getConnection();
        try
        {
            T  objRes = task.exec();

            boolean useTxn = !con.getAutoCommit(); // Así evitamos llamar a commit si no podemos hacerlo pues pudo haberse cambiado en la ejec. de la tarea
            if (useTxn)
                con.commit();

            return objRes;
        }
        catch (Exception ex) // Para cualquier excepción tenemos que hacer el rollback incluidas las RuntimeException
        {
            try
            {
                boolean useTxn = !con.getAutoCommit(); // Así evitamos llamar a rollback si no podemos hacerlo pues pudo haberse cambiado en la ejec. de la tarea
                if (useTxn)
                    con.rollback();
            }
            catch (SQLException ex2) { throw ex2; }

            throw ex; // Constatamos el error hacia fuera
        }
    }
}
