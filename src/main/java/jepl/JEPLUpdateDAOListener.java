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
 * If an implementation of this interface is correctly registered, is used to setup
 * insert/update/delete persistent DAO actions.
 *
 * <p>This listener is only useful in Data Access Object (DAO) persistent updating methods like {@link JEPLDAO#insert(Object)}, {@link JEPLDAO#update(Object)} and {@link JEPLDAO#delete(Object)}.
 * </p>
 *
 * @param <T> the type of the user data model Class to map.
 * @see JEPLListener
 * @see JEPLDAOQuery
 * @author jmarranz
 */
public interface JEPLUpdateDAOListener<T> extends JEPLListener
{
    /**
     * Returns the table associated to the provided type class and user object.
     * 
     * @param jcon the connection wrapper being used.
     * @param obj the user object going to be updated.
     * @return the name of the table.
     * @throws java.lang.Exception       
     */
    public String getTable(JEPLConnection jcon,T obj) throws Exception;
    
    
    /**
     * Returns an array of column descriptor and value pairs used to generate the persistent update action.
     * 
     * <p>When implementing this method you can use java.util.AbstractMap.SimpleEntry</p>
     * 
     * @param jcon the connection wrapper being used.
     * @param obj the user object going to be updated.
     * @param action the persistent action to be executed.
     * @return an array of column descriptor and value pairs
     * @throws java.lang.Exception 
     */
    public Entry<JEPLColumnDesc,Object>[] getColumnDescAndValues(JEPLConnection jcon,T obj,JEPLPersistAction action) throws Exception;
 
}

