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

package jepl;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * This interface represents a factory of Java Transaction API based data sources
 * managed by JEPLLayer.
 * 
 * @author jmarranz
 * @see JEPLBootRoot#createJEPLBootJTA()
 */
public interface JEPLBootJTA extends JEPLBoot
{
    /**
     * Returns the JTA user transaction object defined.
     *
     * @return the JTA user transaction
     * @see #setUserTransaction(UserTransaction)
     */
    public UserTransaction getUserTransaction();

    /**
     * Sets the JTA user transaction object to be used for transactions.
     *
     * @param userTxn the JTA user transaction object
     * @see #getUserTransaction()
     */
    public void setUserTransaction(UserTransaction userTxn);

    /**
     * Returns the JTA user transaction manager defined.
     * 
     * @return the JTA user transaction manager.
     * @see #setTransactionManager(TransactionManager)
     */
    public TransactionManager getTransactionManager();

    /**
     * Sets the JTA user transaction manager object to be used for transactions.
     *
     * @param txnMgr the JTA user transaction manager object
     * @see #getTransactionManager()
     */
    public void setTransactionManager(TransactionManager txnMgr);

    /**
     * Returns the manager useful when you need coordination of multiple JTA data sources.
     *
     * @return the multiple JTA data source manager.
     */
    public JEPLJTAMultipleDataSource getJEPLJTAMultipleDataSource();

    /**
     * Creates a new {@link JEPLDataSource} wrapping the specified JTA capable DataSource.
     *
     * @param ds the DataSource to wrap.
     * @return a new {@link JEPLDataSource}
     */
    public JEPLJTADataSource createJEPLJTADataSource(DataSource ds);

    /**
     * Creates a "fake" JTA UserTransaction based on pure JDBC to be used when
     * there is no true JTA infrastructure.
     *
     * @return a new fake JTA UserTransaction object.
     */
    public UserTransaction createJDBCUserTransaction();
}
