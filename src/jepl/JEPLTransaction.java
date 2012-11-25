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

/**
 * This interface hides the internal actions performed on the Connection to
 * execute a JDBC or JTA transaction.
 *
 * @author jmarranz
 * @see JEPLConnection#getJEPLTransaction()
 */
public interface JEPLTransaction extends JEPLUserData
{
    /**
     * Returns true if the transaction is active.
     */
    public boolean isActive();
    
    /**
     * Begins the transaction.
     */
    public void begin();

    /**
     * Commits the transaction.
     */
    public void commit();

    /**
     * Rollbacks the transaction.
     */
    public void rollback();

    /**
     * Returns the underlying object protagonist of the transaction, the current java.sql.Connection
     * for simple JDBC transactions or javax.transaction.UserTransaction for JTA transactions.
     *
     * @param <T> the expected type.
     * @param type the expected class of the underlying object of the transaction.
     * @return the underlying object of the transaction. 
     */
    public <T> T getUnderlyingTransaction(Class<T> type);
}
