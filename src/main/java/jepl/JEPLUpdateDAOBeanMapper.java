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
 * {@link JEPLUpdateDAOListenerDefault} listener.
 *
 * @param <T> the type of the user data model Class to map.  
 * @author jmarranz
 */
public interface JEPLUpdateDAOBeanMapper<T>
{
    /**
     * Inmutable singleton to indicate a returned value to ignore.
     * 
     * <p>Return this object when implementing {@link #getColumnFromBean(T obj,JEPLConnection jcon,String columnName,Method getter,JEPLPersistAction action)}
     * to indicate there is no custom user value provided, in this case default value obtained from the bean according to {@link JEPLUpdateDAOListenerDefault} is used.</p>
    */
    public static final Object NO_VALUE = new Object();    
    
    /**
     * This method is called when requiring a value to persist or for using as a key when persisting a user defined object 
     *
     * <p>Developer has an opportunity to avoid default mapping behavior doing custom mapping.</p>
     *
     * <p>Calling <code>getter.invoke(obj,(Object[])null);</code> is the same
     * default behavior of {@link JEPLUpdateDAOListenerDefault}.</p>
     *
     * @param obj the user data model object.
     * @param jcon the connection wrapper being used.
     * @param columnName the name of the column to get a value to persist.
     * @param getter the Java reflection getter method of the user data model object found matching the column by name.
     * @param action the persistent action to be executed.
     * @return the value to persist in the provided column. May be {@link #NO_VALUE}.
     * @throws Exception
     */
    public Object getColumnFromBean(T obj,JEPLConnection jcon,String columnName,Method getter,JEPLPersistAction action) throws Exception;
}
