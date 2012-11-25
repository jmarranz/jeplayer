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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import jepl.JEPLException;

/**
 *
 * @author jmarranz
 */
public class UserTransactionJDBC implements UserTransaction
{
    protected ThreadLocal<UserTransactionJDBCOfThread> txnByThread = new ThreadLocal<UserTransactionJDBCOfThread>();
    protected JEPLBootJTAImpl jtaBoot;

    public UserTransactionJDBC()
    {
    }

    public JEPLBootJTAImpl getJEPLBootJTA()
    {
        return jtaBoot;
    }

    public void setJEPLBootJTA(JEPLBootJTAImpl jtaBoot)
    {
        if (this.jtaBoot != null && this.jtaBoot != jtaBoot)
            throw new JEPLException("This UserTransaction object cannot be reused in a different JEPLBootJTA");
        this.jtaBoot = jtaBoot;
    }

    public UserTransactionJDBCOfThread getUserTransactionJDBCOfThread()
    {
        UserTransactionJDBCOfThread txnThread = txnByThread.get();
        if (txnThread == null)
        {
            txnThread = new UserTransactionJDBCOfThread(this);
            txnByThread.set(txnThread);
        }
        return txnThread;
    }

    public void addConnection(Connection con)
    {
        getUserTransactionJDBCOfThread().addConnection(con);
    }

    public void removeConnection(Connection con)
    {
        getUserTransactionJDBCOfThread().removeConnection(con);
    }

    public void begin() throws NotSupportedException, SystemException
    {
        getUserTransactionJDBCOfThread().begin();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
    {
        getUserTransactionJDBCOfThread().commit();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        getUserTransactionJDBCOfThread().rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        getUserTransactionJDBCOfThread().setRollbackOnly();
    }

    public int getStatus() throws SystemException
    {
        return getUserTransactionJDBCOfThread().getStatus();
    }

    public void setTransactionTimeout(int timeout) throws SystemException
    {
        getUserTransactionJDBCOfThread().setTransactionTimeout(timeout);
    }

}
