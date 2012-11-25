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
 * This interface is a wrapper of a JDBC DataSource managed by JEPLayer.
 *
 * @see JEPLBootNonJTA#createJEPLNonJTADataSource(DataSource)
 * @author jmarranz
 */
public interface JEPLNonJTADataSource extends JEPLDataSource
{
    /**
     * Returns the parent object factory of this object.
     *
     * @return the parent factory.
     */
    public JEPLBootNonJTA getJEPLBootNonJTA();

    /**
     * Informs what is the default auto-commit mode of this data source.
     *
     * @return the default auto-commit mode. By default is false.
     */
    public boolean isDefaultAutoCommit();

    /**
     * Sets the default auto-commit mode of this data source.
     *
     * @param value the new default auto-commit mode.
     */
    public void setDefaultAutoCommit(boolean value);

    /**
     * Executes the specified task, a JDBC Connection is got from DataSource in the beginning
     * and released in the end. The task is executed into a transaction if the autoCommit parameter
     * is false.
     *
     * <p>The method {@link JEPLTask#exec()} is called and the return value of this
     * call is the value returned by this method.
     * </p>
     *
     * @param <T> the type of the result value of the task.
     * @param task the task to be executed.
     * @param autoCommit the auto-commit mode.
     * @return the same value returned by the task.
     * @see JEPLDataSource#exec(JEPLTask)
     */
    public <T> T exec(JEPLTask<T> task,boolean autoCommit);
}
