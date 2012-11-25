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

import java.lang.reflect.Method;
import java.sql.SQLException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLException;
import jepl.JEPLTask;
import jepl.JEPLTransactionPropagation;
import jepl.JEPLTransactionalJTA;

/**
 *
 * @author jmarranz
 */
public abstract class TransactionJTAExecutor<T>
{
    public TransactionJTAExecutor()
    {
    }

    public static <T> JEPLTransactionPropagation getJEPLTransactionPropagation(
            JEPLTransactionPropagation txnPropParam,JEPLTask<T> originalTask,JEPLTransactionPropagation txnPropDefault) throws NoSuchMethodException
            
    {
        if (txnPropParam != null) return txnPropParam; // Parámetro explícito, max prioridad
        else
        {
            // Vemos si hay anotación en la task
            Method method = originalTask.getClass().getMethod("exec",(Class[])null);
            JEPLTransactionalJTA txnDec = method.getAnnotation(JEPLTransactionalJTA.class);
            if (txnDec != null)
                return txnDec.propagation();
            else // No hay anotación, el valor por defecto
                return txnPropDefault;
        }
    }

    public abstract T onExecute() throws Exception;

    public T execute(JEPLTransactionPropagation txnProp,UserTransaction txn,TransactionManager txnMgr)
            throws Exception
    {
    /*  javax.transaction.Status
        public static final int STATUS_ACTIVE = 0;
        public static final int STATUS_MARKED_ROLLBACK = 1;
        public static final int STATUS_PREPARED = 2;
        public static final int STATUS_COMMITTED = 3;
        public static final int STATUS_ROLLEDBACK = 4;
        public static final int STATUS_UNKNOWN = 5;
        public static final int STATUS_NO_TRANSACTION = 6;
        public static final int STATUS_PREPARING = 7;
        public static final int STATUS_COMMITTING = 8;
        public static final int STATUS_ROLLING_BACK = 9;
        http://download.oracle.com/javaee/5/api/javax/transaction/Status.html

        http://download.oracle.com/javaee/6/api/javax/ejb/TransactionAttributeType.html

     */

        boolean[] explicitBegin = new boolean[] { false };
        Transaction txnOfTxnMgr = begin(txn,txnMgr,txnProp,explicitBegin);

        try
        {
            T objRes = onExecute();

            commit(txn,explicitBegin[0]);

            return objRes;
        }
        catch(Exception ex) // Para cualquier excepción tenemos que hacer el rollback incluidas las RuntimeException
        {
            rollback(txn,explicitBegin[0]);

            throw ex;
        }
        finally
        {
            if (txnOfTxnMgr != null)
                txnMgr.resume(txnOfTxnMgr);
        }
    }

    public static Transaction begin(UserTransaction txn,TransactionManager txnMgr,JEPLTransactionPropagation txnProp,boolean[] explicitBegin) throws Exception
    {
        Transaction txnOfTxnMgr = null;
        int status = txn.getStatus();
        if (status == Status.STATUS_NO_TRANSACTION)
        {
            switch(txnProp)
            {
                case MANDATORY: throw new JEPLException("Specified MANDATORY transaction propagation mode and there is no active transaction");
                case NEVER: return null; // No hay transacción activa y no queremos transacción
                case NOT_SUPPORTED: return null; // No hay transacción activa y no queremos transacción, no hay transacción que suspender
                case REQUIRED: break; // No hay transacción y la necesitamos el begin() se ejecutará
                case REQUIRES_NEW: break; // No hay transacción y la necesitamos el begin() se ejecutará
                case SUPPORTS: return null; // En caso de no haber transacción activa es el mismo caso que NOT_SUPPORTED
                // No hay más casos
            }

            txn.begin();
            explicitBegin[0] = true;
        }
        else if (status == Status.STATUS_ACTIVE)
        {
            switch(txnProp)
            {
                case MANDATORY: break; // Bien ya hay una transacción activa
                case NEVER:
                    throw new JEPLException("Specified NEVER transaction propagation mode and there is an active transaction");
                case NOT_SUPPORTED:
                    if (txnMgr != null)
                    {
                        txnOfTxnMgr = txnMgr.suspend();
                        break;
                    }
                    else throw new JEPLException("Specified NOT_SUPPORTED transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)");
                case REQUIRED: break; // Nada que hacer hay ya una transacción abierta
                case REQUIRES_NEW:
                    if (txnMgr != null)
                    {
                        txnOfTxnMgr = txnMgr.suspend();
                        txn.begin(); // Iniciamos una nueva txn
                        explicitBegin[0] = true;
                        break;
                    }
                    else throw new JEPLException("Specified REQUIRES_NEW transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)");
                case SUPPORTS: break; // En caso de haber transacción activa es el mismo caso que REQUIRED
                // No hay más casos
            }

            // No llamamos a begin() porque la transacción está ya en marcha
        }
        else
        {
            throw new JEPLException("UserTransaction in illegal/unknown state: " + status);
        }

        return txnOfTxnMgr;
    }

    public static void commit(UserTransaction txn,boolean explicitBegin) throws Exception
    {
        int status = txn.getStatus();
        if (status == Status.STATUS_ACTIVE)
        {
            if (explicitBegin) // Hicimos nosotros el begin podemos terminar la transacción
                txn.commit();
        }
        else if (status == Status.STATUS_NO_TRANSACTION ||
                status == Status.STATUS_COMMITTED  ||
                status == Status.STATUS_ROLLEDBACK)
        {
            // Nada que hacer ya ha habido commit o rollback
        }
        else
        {
            throw new JEPLException("UserTransaction in illegal/unknown state: " + status);
        }
    }

    public static void rollback(UserTransaction txn,boolean explicitBegin) throws Exception
    {
        int status = txn.getStatus();
        if (status == Status.STATUS_ACTIVE)
        {
            if (explicitBegin) // Hicimos nosotros el begin podemos terminar la transacción
                txn.rollback();
        }
        else if (status == Status.STATUS_NO_TRANSACTION ||
                status == Status.STATUS_COMMITTED  ||
                status == Status.STATUS_ROLLEDBACK)
        {
            // Nada que hacer ya ha habido commit o rollback
        }
        else
        {
            throw new JEPLException("UserTransaction in illegal/unknown state: " + status);
        }
    }
}
