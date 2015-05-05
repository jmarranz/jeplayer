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
 * Default implementation of this interface is used by the framework to provided
 * a built-in ready mapping between ResultSet and user defined objects
 * following Java Bean patterns (properties).
 * 
 * @param <T> the type of elements of this result set
 * @see JEPLDataSource#createJEPLResultSetDAOListenerDefault(Class)
 * @see JEPLDataSource#createJEPLResultSetDAOListenerDefault(Class,JEPLResultSetDAOBeanMapper)
 * @author jmarranz
 */
public interface JEPLResultSetDAOListenerDefault<T> extends JEPLResultSetDAOListener<T>
{
    /**
     * Returns the bean class of the user defined objects to map.
     *
     * @return the bean class.
     */
    public Class<T> getBeanClass();

    /**
     * Returns the optional row-bean mapper to modify the default behavior of this listener
     * for properties which do not fit into the default mapping.
     *
     * @return the optional row-bean mapper. May be null.
     */
    public JEPLResultSetDAOBeanMapper<T> getJEPLResultSetDAOBeanMapper();
}
