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
 * the JDBC ResultSet before iterating it and to get values with the desired data type.
 *
 * <p>This listener is only useful in Data Access Layer (DAL) persistent methods, that is,
 * methods which do not return user data model objects.
 * </p>
 *
 * @see JEPLListener
 * @see JEPLDALQuery
 * @author jmarranz
 */
public interface JEPLResultSetDALListener extends JEPLListener
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
     * This method is called to get the appropriated value from the JDBC ResultSet
     * of the current row and specified column.
     *
     * @param <U> the expected type.
     * @param columnIndex the column index. Starting in 1.
     * @param returnType the expected Class type.
     * @param jrs the wrapper of the ResultSet
     * @return the value with the expected data type of current row and specified column.
     * @throws Exception
     */
    public <U> U getValue(int columnIndex,Class<U> returnType,JEPLResultSet jrs) throws Exception;
}
