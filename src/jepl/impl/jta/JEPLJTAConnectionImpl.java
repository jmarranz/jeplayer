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

import java.sql.Connection;
import jepl.JEPLConnectionListener;
import jepl.JEPLException;
import jepl.JEPLTransactionPropagation;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLListenerListImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLJTAConnectionImpl extends JEPLConnectionImpl
{
    public JEPLJTAConnectionImpl(JEPLJTADataSourceImpl ds,Connection con)
    {
        super(ds,con);
    }

    public JEPLJTADataSourceImpl getJEPLJTADataSource()
    {
        return (JEPLJTADataSourceImpl)ds;
    }

    public JEPLCurrentTransactionJTAImpl getJEPLCurrentTransactionJTA()
    {
        return (JEPLCurrentTransactionJTAImpl)currentTxn;
    }    

    public <T> T execTask(JEPLTaskExecContextInConnectionJTAImpl<T> taskCtx,JEPLDALImpl dal,JEPLListenerListImpl paramListener,JEPLTransactionPropagation txnProp) throws Exception
    {
        JEPLConnectionListener<T> listener = getJEPLConnectionListener(dal,paramListener);

        JEPLTaskOneExecWithConnectionImpl<T> task = taskCtx.getJEPLTaskWithJEPLConnection();

        this.currentTxn = new JEPLCurrentTransactionJTAImpl(this);
        if (listener != null) listener.setupJEPLConnection(this, task);

        if (task.isExecuted())
        {
            // En este caso el programador o bien nuestro JEPLConnectionListener es el responsable del control de la transacción
            // (commit y rollback) si no lo ha hecho es problema suyo
            return task.getResult();
        }
        else
        {
            if (currentTxn.isUsed()) // El programador ha usado la JEPLTransaction pero extrañamente no ha llamado a exec() del task
                throw new JEPLException("You have used the JEPLTransaction but the next task was not executed");

            // El programador pudo haber definido un JEPLConnectionListener y demarcar
            // la transacción manualmente (ejecutando el task) pero no lo ha hecho
            // por lo que lo único que queda es ejecutar la tarea
            return task.exec();
        }
    }
}
