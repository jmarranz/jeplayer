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

import java.util.Map.Entry;

/**
 * {@link TO DO}
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
public interface JEPLUpdateDAOListener<T> extends JEPLListener
{
    /**
     * {@link TO DO}
     * @param jcon
     * @param obj
     * @return 
     */
    public String getTable(JEPLConnection jcon,T obj);
    
    
    /**
     * {@link TO DO}
     * 
     * <p>When implementing this method you can use java.util.AbstractMap.SimpleEntry</p>
     * 
     * @param jcon
     * @param obj
     * @param action
     * @return 
     * @throws java.lang.Exception 
     */
    public Entry<JEPLColumnDesc,Object>[] getColumnDescAndValues(JEPLConnection jcon,T obj,JEPLPersistAction action) throws Exception;
 
}

