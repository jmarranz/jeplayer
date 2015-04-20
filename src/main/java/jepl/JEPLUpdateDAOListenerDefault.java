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
public interface JEPLUpdateDAOListenerDefault<T> extends JEPLUpdateDAOListener<T>
{
    /**
     * Returns the bean class of the user defined object to map.
     *
     * @return the bean class.
     */
    public Class<T> getBeanClass();
    
    /**
     * {@link TO DO}
     * @return 
     */
    public JEPLUpdateDAOBeanMapper<T> getJEPLUpdateDAOBeanMapper();    
}
