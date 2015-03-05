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
 * The implementation of this inteface provides a way to manage in the same time several
 * JTA DataSources usually to perform distributed transactions with several DataSource involved.
 *
 * <p>As you know you can create several different {@link JEPLJTADataSource} objects wrapping
 * different java.sql.DataSource through the same {@link JEPLBootJTA} instance, 
 * JEPLayer saves internally all of these {@link JEPLJTADataSource} objects because there is
 * an option to manage these {@link JEPLJTADataSource} objects in the same time by using the
 * associated {@link JEPLJTAMultipleDataSource} to the {@link JEPLBootJTA} instance.
 * </p>
 *
 * <p>Because this interface is mainly used to execute distributed transactions involving
 * all of {@link JEPLJTADataSource} associated in the same time, the API is very similar to {@link JEPLJTADataSource}.
 * </p>
 *
 *
 * @see JEPLBootJTA#getJEPLJTAMultipleDataSource()
 * @author jmarranz
 */
public interface JEPLJTAMultipleDataSource
{
    /**
     * Returns the parent object factory of this object.
     *
     * @return the parent factory.
     */
    public JEPLBootJTA getJEPLBootJTA();
    
    /**
     * Returns the default transaction propagation criterion for this multiple data source manager.     
     *
     * @return the default transaction propagation. By default is {@link JEPLTransactionPropagation#REQUIRED}
     * @see #setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation)
     */
    public JEPLTransactionPropagation getDefaultJEPLTransactionPropagation();

    /**
     * Sets the default transaction propagation criterion for this multiple data source manager.
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
     * and released in the end from all of DataSource managed by this object.
     * The task is executed into a transaction if a JTA transaction is active
     * according to the context of this call (if an active transaction already exists) and the propagation parameter.
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @param txnProp the transaction propagation to be applied, if null the default propagation of this object is applied.
     * @return the same value returned by the task.
     * @see #getDefaultJEPLTransactionPropagation()
     */
    public <T> T exec(JEPLTask<T> task,JEPLTransactionPropagation txnProp);

    /**
     * Executes the specified task (see {@link #exec(JEPLTask,JEPLTransactionPropagation)}).
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @return the same value returned by the task.
     * @see #exec(JEPLTask,JEPLTransactionPropagation)
     */
    public <T> T exec(JEPLTask<T> task);
}
