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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLException;
import jepl.JEPLTransaction;
import jepl.impl.JEPLCurrentTransactionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLCurrentTransactionJTAImpl extends JEPLCurrentTransactionImpl implements JEPLTransaction
{
    public JEPLCurrentTransactionJTAImpl(JEPLJTAConnectionImpl conWrap)
    {
        super(conWrap);
    }

    public JEPLJTAConnectionImpl getJEPLJTAConnection()
    {
        return (JEPLJTAConnectionImpl)conWrap;
    }

    public UserTransaction getUserTransaction()
    {
        return getJEPLJTAConnection().getJEPLJTADataSource().getUserTransaction();
    }

    public TransactionManager getTransactionManager()
    {
        return getJEPLJTAConnection().getJEPLJTADataSource().getTransactionManager();
    }    

    @SuppressWarnings("unchecked")
    public <T> T getUnderlyingTransaction(Class<T> type)
    {
        return (T)getUserTransaction();
    }

    @Override
    public boolean isActive()
    {
        try
        {
            return getUserTransaction().getStatus() == Status.STATUS_ACTIVE;
        }
        catch(SystemException ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public void begin()
    {
        super.begin();
       
        try
        {
             getUserTransaction().begin();
        }
        catch(NotSupportedException ex)
        {
            throw new JEPLException(ex);
        }
        catch(SystemException ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public void commit()
    {
        super.commit();
        
        try
        {
            getUserTransaction().commit();
        }
        catch(RollbackException ex)
        {
            throw new JEPLException(ex);
        }
        catch(HeuristicMixedException ex)
        {
            throw new JEPLException(ex);
        }
        catch(HeuristicRollbackException ex)
        {
            throw new JEPLException(ex);
        }
        catch(SystemException ex)
        {
            throw new JEPLException(ex);
        }
        finally
        {
            endTxn();
        }
    }

    @Override
    public void rollback()
    {
        super.rollback();
        
        try
        {
            getUserTransaction().rollback();
        }
        catch(SystemException ex)
        {
            throw new JEPLException(ex);
        }
        finally
        {
            endTxn();
        }
    }

    public void endTxn()
    {
    }
}
