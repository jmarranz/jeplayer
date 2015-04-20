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

import java.lang.reflect.Method;

/**
 * The interface of the optional row-bean mapper to modify the default mapping behavior of a 
 * {@link JEPLResultSetDAOListenerDefault} listener.
 *
 * @param <T> the type of the user data model Class to map.  
 * @author jmarranz
 */
public interface JEPLResultSetDAOBeanMapper<T>
{
    /**
     * This method is called when trying to map the value got from a column of a ResultSet
     * to a property specified by the parameter setter of the user data model object provided (parameter obj)
     *
     * <p>Programmer has an opportunity to avoid default mapping behavior doing custom mapping
     * and returning true.</p>
     *
     * <p>Calling <code>setter.invoke(obj, new Object[] { value }); </code> is the same
     * as default behavior of {@link JEPLResultSetDAOListenerDefault}.</p>
     *
     * @param obj the user data model object.
     * @param jrs the ResultSet wrapper.
     * @param col the column of the ResultSet to get. Starting in 1.
     * @param columnName the name of the column of the ResultSet to get.
     * @param value the value got from ResultSet column proposed to set in user data model object.
     * @param setter the Java reflection setter method of the user data model object found matching the column by name. May be null (not found setter).
     * @return true if developer has "manually" mapped this column-property (no default mapping is done).
     */
    public boolean setColumnInBean(T obj,JEPLResultSet jrs,int col,String columnName,Object value,Method setter);
}
