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
 * If an implementation of this interface is correctly registered, is used to setup
 * the JDBC ResultSet before iterating and to get values with the correct data type.
 *
 * <p>This listener is only useful in Data Access Object (DAO) persistent methods, that is,
 * methods which return data model POJOs.
 * </p>
 *
 * @param <T> the type of elements of this result set
 * @see JEPLListener
 * @see JEPLDAOQuery
 * @author jmarranz
 */
public interface JEPLResultSetDAOListener<T> extends JEPLListener
{
    /**
     * This method is called before iterating the JDBC ResultSet.
     *
     * @param jrs the ResultSet wrapper.
     * @param task represents the consecutive task to be executed (iteration through results).
     * @throws Exception
     */
    public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception;

    /**
     * This method is called when reading a row to create the data model object to be "loaded".
     *
     * @param jrs the wrapper of the ResultSet.
     * @return the new data model object, can be null (row skipped).
     * @throws Exception
     */
    public T createObject(JEPLResultSet jrs) throws Exception;

    /**
     * This method is called to fill with database data of the current row the provided data model object.
     *
     * <p>The data model object is ever non-null and created by a previous call to
     * {@link #createObject(jepl.JEPLResultSet)}.
     * </p>
     *
     * @param obj the data model object to fill with data base data of the current row.
     * @param jrs the wrapper of the ResultSet.
     * @throws Exception
     */
    public void fillObject(T obj,JEPLResultSet jrs) throws Exception;
}
