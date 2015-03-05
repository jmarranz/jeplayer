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
 * This enumeration describes the options of the transaction propagation of a JTA 
 * transaction through nested persistent tasks.
 *
 * <p>Semantics and names are borrowed from Java EE standard <a href="http://download.oracle.com/javaee/6/api/javax/ejb/TransactionAttributeType.html">TransactionAttributeType</a>.</p>
 *
 * <p>javax.transaction.UserTransaction is used to create new transactions.</p>
 * 
 * @author jmarranz
 */
public enum JEPLTransactionPropagation
{
    /** A JTA transaction must exist otherwise an exception is thrown. */
    MANDATORY,

    /** If a JTA transaction exists is used, otherwise a new transaction is created */
    REQUIRED,

    /** If there is no JTA transaction a new one is created, otherwise the current transaction
     is suspended in the caller thread and a new transaction is started until the task ends
     then the suspended transaction is resumed.
     */
    REQUIRES_NEW,

    /** If a JTA transaction exists behavior is the same as {@link #REQUIRED} otherwise
     behavior is the same as {@link #NOT_SUPPORTED}. In summary the task is executed into
     a transaction if previously was present. */
    SUPPORTS,

    /** If there is a JTA transaction the current transaction
     is suspended in the caller thread, no new transaction is started and when the task ends
     the suspended transaction is resumed. If there is no JTA transaction, no new transaction
     * is created.
     */
    NOT_SUPPORTED,

    /** If a JTA transaction already exists an exception is thrown otherwise no transaction is used. */
    NEVER;
}
