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
 * This interface defines the Data Access Object (DAO) level, used to execute queries returning
 * user defined data model objects filled with database data.
 *
 * @param <T> the type of the user data model Class to map.
 * @see JEPLDataSource#createJEPLDAO(Class)
 * @author jmarranz
 */
public interface JEPLDAO<T> extends JEPLDAL
{
    /**
     * Creates a utility object to perform database queries using a fluid API returning
     * user defined data model objects.
     *
     * @param sql the SQL statement.
     * @return the utility object to execute database queries.
     */
    public JEPLDAOQuery<T> createJEPLDAOQuery(String sql);
    
    /**
     * {@link TODO}
     * 
     * @param obj
     * @return 
     */
    public JEPLDAOQuery<T> insert(T obj);
    
    /**
     * {@link TODO}
     * 
     * @param obj
     * @return 
     */
    public JEPLDAOQuery<T> update(T obj);   
    
    /**
     * {@link TODO}
     * 
     * @param obj
     * @return 
     */
    public JEPLDAOQuery<T> delete(T obj);     
}
