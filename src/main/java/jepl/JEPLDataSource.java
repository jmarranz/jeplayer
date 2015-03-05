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

/**
 * This interface is a wrapper of a standard JDBC DataSource managed by JEPLayer.
 *
 * @author jmarranz
 */
public interface JEPLDataSource extends JEPLUserData
{
    /**
     * Returns the parent object factory of this object.
     *
     * @return the parent factory.
     */
    public JEPLBoot getJEPLBoot();

    /**
     * Returns the wrapped DataSource.
     *
     * @return the wrapped DataSource.
     */
    public DataSource getDataSource();

    /**
     * Returns whether any PreparedStatement object used is cached for several
     * sentences when the SQL code is the same.
     * 
     * <p>If this method returns true, java.sql.PreparedStatement objects are reused when possible
     * (when the SQL sentence is the same), in this case be careful with PreparedStatement
     * configuration calling methods like setMaxRows or setFetchSize because the same
     * PreparedStatement object is reused to perform several queries. You can restore
     * to the default state inmediately after executing every query (do not mind of threads because by design
     * a PreparedStatement cannot be used for several concurrent threads and JEPLayer respects
     * this JDBC contract).
     * </p>
     *
     * 
     * @return true if PreparedStatement objects are cached. By default is true.
     * @see #setPreparedStatementCached(boolean)
     */
    public boolean isPreparedStatementCached();

    /**
     * Sets whether any PreparedStatement object used is cached for several
     * sentences when the SQL code is the same.
     *
     * @param value true for caching statements.
     * @see #isPreparedStatementCached()
     */
    public void setPreparedStatementCached(boolean value);

    /**
     * Returns the current connection being used by this thread in the time of calling.
     *
     * @return the current connection of this thread. Null if there is no connection in use.
     */
    public JEPLConnection getCurrentJEPLConnection();

    /**
     * Registers a lifecycle listener associated to this object.
     *
     * <p>If the provided listener implements several JEPLayer interfaces, this method
     * registers all of them.</p>
     *
     * <p>This method can be called several times. Only one listener of the same type is
     * registered replacing previous registries.</p>
     *
     * @param listener the lifecycle listener object.
     */
    public void addJEPLListener(JEPLListener listener);

    /**
     * Unregisters the specified listener.
     *
     * @param listener the listener to remove.
     */
    public void removeJEPLListener(JEPLListener listener);

    /**
     * Creates a new standalone DAL object associated to this data source.
     *
     * @return the new standalone DAL object.
     */
    public JEPLDAL createJEPLDAL();

    /**
     * Creates a new standalone DAO object associated to this data source.
     *
     * @param type the Class type of user defined data model objects of the new DAO object.
     * @return the new standalone DAO object.
     */
    public <T> JEPLDAO<T> createJEPLDAO(Class<T> type);

    /**
     * Creates a default {@link JEPLResultSetDAOListener} bean providing automatic mapping
     * between your user data model objects and data base rows.
     *
     * @param <T> the type of the user data model Class to map.
     * @param clasz the class of the user data model Class to map.
     * @param mapper optional mapper (may be null) to change default mapping behavior for concrete properties.
     * @return a new result set mapper bean.
     */
    public <T> JEPLResultSetDAOListenerDefault<T> createJEPLResultSetDAOListenerDefault(Class<T> clasz,JEPLRowBeanMapper<T> mapper);

    /**
     * Creates a default {@link JEPLResultSetDAOListener} bean providing automatic mapping
     * between your user data model objects and data base rows.
     *
     * @param <T> the type of the user data model Class to map.
     * @param clasz the class of the user data model Class to map.
     * @return a new result set mapper bean.
     * @see #createJEPLResultSetDAOListenerDefault(Class,JEPLRowBeanMapper)
     */
    public <T> JEPLResultSetDAOListenerDefault<T> createJEPLResultSetDAOListenerDefault(Class<T> clasz);


    /**
     * Executes the specified task, a JDBC Connection is got from DataSource in the beginning
     * and released in the end.
     *
     * <p>If this object is a {@link JEPLJTADataSource} the transactional behavior of the task
     * is defined by the annotation {@link JEPLTransactionalJTA} if present in the {@link JEPLTask#exec()}
     * of the {@link JEPLTask} parameter, if not present the default transaction propagation
     * returned by {@link JEPLJTADataSource#getDefaultJEPLTransactionPropagation()} is applied.
     * </p>
     *
     * <p>If this object is a {@link JEPLNonJTADataSource} the transactional behavior of the task
     * is defined by the annotation {@link JEPLTransactionalNonJTA} if present in the {@link JEPLTask#exec()}
     * of the {@link JEPLTask} parameter, if not present the default auto-commit mode
     * returned by {@link JEPLNonJTADataSource#isDefaultAutoCommit()} is applied.
     * </p>
     *
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @return the same value returned by the task.
     */
    public <T> T exec(JEPLTask<T> task);

    /**
     * Executes the specified task, a JDBC Connection is got from DataSource in the beginning
     * and released in the end.
     *
     * <p>The optional listener provided as parameter can modify the transactional behavior
     * of the task, see {@link JEPLConnectionListener}.</p>
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @param listener an optional listener. May be null.
     * @return the same value returned by the task.
     * @see #exec(JEPLTask) 
     */
    public <T> T exec(JEPLTask<T> task,JEPLListener listener);

}
