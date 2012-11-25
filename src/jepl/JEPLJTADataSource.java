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
 * This interface is a wrapper of a JTA capable and registered in JTA infrastructure
 * JDBC DataSource managed by JEPLayer.
 *
 * @see JEPLBootJTA#createJEPLJTADataSource(DataSource)
 * @author jmarranz
 */
public interface JEPLJTADataSource extends JEPLDataSource
{
    /**
     * Returns the parent object factory of this object.
     *
     * @return the parent factory.
     */
    public JEPLBootJTA getJEPLBootJTA();
       
    /**
     * Returns the default transaction propagation criterion for this data source.
     *specified

     * <p>Default value is specified by {@link JEPLJTAMultipleDataSource#getDefaultJEPLTransactionPropagation()}.</p>
     *
     * @return the default transaction propagation.
     * @see #setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation)
     */
    public JEPLTransactionPropagation getDefaultJEPLTransactionPropagation();

    /**
     * Sets the default transaction propagation criterion for this data source.
     * 
     * @param defaultTransactionPropagation the default transaction propagation.
     * @see #getDefaultJEPLTransactionPropagation()
     */
    public void setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation defaultTransactionPropagation);
   
    /**
     * Returns the current transaction propagation being used by this thread in the time of calling.
     *
     * @return the current transaction propagation of this thread.
     */
    public JEPLTransactionPropagation getCurrentJEPLTransactionPropagation();


    /**
     * Executes the specified task, a JDBC Connection is got from DataSource in the beginning
     * and released in the end. The task is executed into a transaction if a JTA transaction is active
     * according to the context of this call (if an active transaction already exists) and the propagation parameter.
     *
     * <p>The optional listener provided as parameter can modify the transactional behavior
     * of the task, see {@link JEPLConnectionListener}.</p>
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @param listener an optional listener. May be null.
     * @param txnProp the transaction propagation to be applied, if null the default propagation of this object is applied.
     * @return the same value returned by the task.
     * @see #getDefaultJEPLTransactionPropagation()
     */
    public <T> T exec(JEPLTask<T> task,JEPLListener listener,JEPLTransactionPropagation txnProp);

    /**
     * Executes the specified task (see {@link #exec(JEPLTask,JEPLListener,JEPLTransactionPropagation)}).
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @param txnProp the transaction propagation to be applied, if null the default propagation of this object is applied.
     * @return the same value returned by the task.
     * @see #exec(JEPLTask,JEPLListener,JEPLTransactionPropagation)
     */
    public <T> T exec(JEPLTask<T> task,JEPLTransactionPropagation txnProp);
}
