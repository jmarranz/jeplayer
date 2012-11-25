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
 * This interface defines the Data Access Layer (DAL) level, used to execute generic queries to the database.
 *
 * @see JEPLDataSource#createJEPLDAL()
 * @see JEPLDAO
 * @author jmarranz
 */
public interface JEPLDAL extends JEPLUserData
{
    /**
     * Returns the DataSource wrapper parent of this object.
     *
     * @return the DataSource wrapper parent.
     */
    public JEPLDataSource getJEPLDataSource();

    /**
     * Registers a lifecycle listener associated to this DAL object.
     *
     * <p>Behavior is the same as {@link JEPLDataSource#addJEPLListener(JEPLListener)}
     * but in this case listeners are only used in this level.
     * </p>
     *
     * @param listener the lifecycle listener object.
     */
    public void addJEPLListener(JEPLListener listener);

    /**
     * Unregisters the specified listener.
     *
     * <p>If the provided listener implements several JEPLayer interfaces, this method
     * unregisters all of them.</p>
     *
     * @param listener the listener to remove.
     */
    public void removeJEPLListener(JEPLListener listener);

    /**
     * This method is called when the framework needs to convert a value
     * to the required data type.
     *
     * @param <U> the required type.
     * @param obj the value to be converted.
     * @param returnType the required Class type.
     * @return the converted value.
     * @see JEPLDALQuery#getGeneratedKey(java.lang.Class)
     * @see JEPLDALQuery#getOneRowFromSingleField(java.lang.Class)
     */
    public <U> U cast(Object obj,Class<U> returnType);

    /**
     * Creates a utility object to perform database queries using a fluid API.
     * 
     * @param sql the SQL statement.
     * @return the utility object to execute database queries.
     */
    public JEPLDALQuery createJEPLDALQuery(String sql);
}
