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
import java.sql.SQLException;
import java.util.LinkedList;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import jepl.JEPLException;

/**
 *
 * @author jmarranz
 */
public class UserTransactionJDBCOfThread
{
    protected int status = Status.STATUS_NO_TRANSACTION;
    protected LinkedList<Connection> conList = new LinkedList<Connection>();
    protected boolean rollbackOnly = false;
    protected UserTransactionJDBC parent;

    public UserTransactionJDBCOfThread(UserTransactionJDBC parent)
    {
        this.parent = parent;
    }

    public void addConnection(Connection con)
    {
        conList.addLast(con);
    }

    public void removeConnection(Connection con)
    {
        Connection removed = conList.removeFirst(); // Suponemos que iteramos en el mismo orden que añadimos
        if (con != removed)
            throw new JEPLException("INTERNAL ERROR");
    }
    
    public void begin() throws NotSupportedException, SystemException
    {
        try
        {
            for(Connection con : conList)
                con.setAutoCommit(false);
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
        this.status = Status.STATUS_ACTIVE;
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
    {
        if (rollbackOnly) throw new RollbackException();
        int count = 0;
        try
        {

            for(Connection con : conList)
            {
                con.commit();
                count++;
            }
        }
        catch(SQLException ex)
        {
            throw new JEPLException("Some commit operation has failed, remaining to commit " + (conList.size() - count) + " connections",ex);
        }
        this.status = Status.STATUS_COMMITTED;

        endTransaction();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        int count = 0;
        try
        {
            for(Connection con : conList)
            {
                con.rollback();
                count++;
            }
        }
        catch(SQLException ex)
        {
            throw new JEPLException("Some rollback operation has failed, remaining to rollback " + (conList.size() - count) + " connections",ex);
        }
        this.status = Status.STATUS_ROLLEDBACK;

        endTransaction();
    }

    protected void endTransaction() throws SystemException
    {
        try
        {
            for(Connection con : conList)
                con.setAutoCommit(true);
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }

        this.rollbackOnly = false; // Volvemos a poner el valor por defecto
        //setTransactionTimeout(0);

        this.status = Status.STATUS_NO_TRANSACTION;     
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        this.rollbackOnly = true;
    }

    public int getStatus() throws SystemException
    {
        return status;
    }

    public void setTransactionTimeout(int timeout) throws SystemException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
