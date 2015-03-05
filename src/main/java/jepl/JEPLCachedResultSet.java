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
 * This interface is very similar to JDBC ResultSet but it is disconnected, it contains all of results
 * of the query.
 *
 * @see JEPLDALQuery#getJEPLCachedResultSet()
 * @author jmarranz
 */
public interface JEPLCachedResultSet
{
    /**
     * Returns the column names used in this query.
     *
     * @return an array with the column names.
     */
    public String[] getColumnLabels();
    
    /**
     * Returns the number of columns of this query.
     *
     * @return the number of columns.
     */    
    public int getColumnCount();
    
    /**
     * Returns the index number of the column name provided.
     *
     * @param columnLabel the name of the column.
     * @return the index number of the column.
     */
    public int getColumIndex(String columnLabel);

    /**
     * Returns the column name associated to the column index provided.
     *
     * @param columnIndex the index of the column. Starting in 1.
     * @return the index number of the column.
     */
    public String getColumnLabel(int columnIndex);

    /**
     * Returns the number of results.
     *
     * @return the number of results.
     */
    public int size();

    /**
     * Returns the result value of the specified row and column.
     *
     * @param <T> the expected type.
     * @param row the row index, starting in 1.
     * @param columnIndex the column index, starting in 1.
     * @param type the Class type expected.
     * @return the result value.
     */
    public <T> T getValue(int row,int columnIndex,Class<T> type);
    
    /**
     * Returns the result value of the specified row and column.
     *
     * @param <T> the expected type.
     * @param row the row index, starting in 1.
     * @param columnLabel the column name.
     * @param type the Class type expected.
     * @return the result value.
     */
    public <T> T getValue(int row,String columnLabel,Class<T> type);

    /**
     * Returns the result object of the specified row and column.
     *
     * @param row the row index, starting in 1.
     * @param columnIndex the column index, starting in 1.
     * @return the result object.
     */
    public Object getObject(int row,int columnIndex);

    /**
     * Returns the result object of the specified row and column.
     *
     * @param row the row index, starting in 1.
     * @param columnLabel the column name.
     * @return the result object.
     */
    public Object getObject(int row,String columnLabel);
}
