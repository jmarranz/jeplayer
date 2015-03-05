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

package example.jndi;

import example.DataSourceLoaderImpl;
import example.DataSourceLoaderJTA;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 *
 * @author jmarranz
 */
public class DataSourceLoaderJTAJNDI extends DataSourceLoaderImpl implements DataSourceLoaderJTA
{
    protected DataSource ds;

    public DataSourceLoaderJTAJNDI(String jndiName)
    {
        try
        {
            Context ctx = new InitialContext();
            this.ds = (DataSource) ctx.lookup( jndiName );
        }
        catch (NamingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getName()
    {
        return "JTAJNDI";
    }


    @Override
    public DataSource getDataSource()
    {
        return ds;
    }

    @Override
    public void destroy()
    {
    }

    public UserTransaction getUserTransaction()
    {
        try
        {
            Context ctx = new InitialContext();
            UserTransaction userTxn = (UserTransaction)ctx.lookup("java:comp/UserTransaction");
            return userTxn;
        }
        catch (NamingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public TransactionManager getTransactionManager()
    {
        try
        {
            Context ctx = new InitialContext();
            TransactionManager txnMgr = (TransactionManager)ctx.lookup("java:appserver/TransactionManager");
            return txnMgr;
        }
        catch (NamingException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
