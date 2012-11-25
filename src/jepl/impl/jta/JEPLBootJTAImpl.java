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
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLException;
import jepl.JEPLJTADataSource;
import jepl.JEPLJTAMultipleDataSource;
import jepl.impl.JEPLBootImpl;
import jepl.impl.JEPLBootRootImpl;

/**
 * {@HACER}
 * 
 * @author jmarranz
 */
public class JEPLBootJTAImpl extends JEPLBootImpl implements JEPLBootJTA
{
    protected boolean inUse = false;
    protected UserTransaction userTxn;
    protected TransactionManager txnMgr; // Opcional, puede quedar a null
    protected JEPLJTAMultipleDataSourceImpl dataSourceMgr = new JEPLJTAMultipleDataSourceImpl(this);

    public JEPLBootJTAImpl(JEPLBootRootImpl root)
    {
        super(root);
    }

    public JEPLJTAMultipleDataSourceImpl getJEPLJTAMultipleDataSourceImpl()
    {
        return dataSourceMgr;
    }
    
    public JEPLJTAMultipleDataSource getJEPLJTAMultipleDataSource()
    {
        return getJEPLJTAMultipleDataSourceImpl();
    }

    public UserTransaction getUserTransaction()
    {
        return userTxn;
    }

    public void setUserTransaction(UserTransaction userTxn)
    {
        if (inUse) throw new JEPLException("Some JEPLJTADataSource has been created");

        if (userTxn instanceof UserTransactionJDBC)
            ((UserTransactionJDBC)userTxn).setJEPLBootJTA(this);        

        this.userTxn = userTxn;
        if (txnMgr == null && (userTxn instanceof TransactionManager)) // Se da por ej en JOTM http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/transaction/jta/JotmFactoryBean.html
            this.txnMgr = (TransactionManager)userTxn;
        // Puede quedar a null
    }

    public TransactionManager getTransactionManager()
    {
        return txnMgr;
    }

    public void setTransactionManager(TransactionManager txnMgr)
    {
        if (inUse) throw new JEPLException("Some JEPLJTADataSource has been created");
        this.txnMgr = txnMgr;
    }
    
    public JEPLJTADataSource createJEPLJTADataSource(DataSource ds)
    {
        if (userTxn == null)
            throw new JEPLException("The UserTransaction object must be defined first");
        this.inUse = true;
        JEPLJTADataSourceImpl jds;
        if (userTxn instanceof UserTransactionJDBC)
            jds = new JEPLJTADataSourceForUserTxnJDBCImpl(this,ds);
        else
            jds = new JEPLJTADataSourceDefaultImpl(this,ds);
        dataSourceMgr.addJEPLJTADataSource(jds);
        return jds;
    }
   
    public UserTransaction createJDBCUserTransaction()
    {
        return new UserTransactionJDBC();
    }
}
