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
 * Utility class just to save user auxiliary data by name.
 *
 * <p>Implementation of this class is thread safe when the object can be a singleton.</p>
 *
 * <p>Most of JEPLayer objects implement this interface.</p>
 * 
 * @author jmarranz
 */
public interface JEPLUserData
{
    /**
     * Returns all registered names.
     *
     * @return an array with all registered names.
     */
    public String[] getUserDataNames();
    /**
     * Informs whether the registry contains one pair name/value with the specified name.
     *
     * @param name the name to look for.
     * @return true if there is a name/value pair with this name.
     */    
    public boolean containsName(String name);

    /**
     * Returns the value associated to the specified name.
     *
     * @param name the name to look for.
     * @return the value associated or null if not found.
     */
    public Object getUserData(String name);
    /**
     * Returns the value associated to the specified name.
     *
     * @param name the name to look for.
     * @param returnType expected data type.
     * @return the value associated or null if not found.
     */
    public <T> T getUserData(String name,Class<T> returnType);
    /**
     * Sets a new value associated to the specified name.
     *
     * @param name the name used to register.
     * @param value the value with the specified name.
     * @return the old value associated to this name or null if none.
     */
    public Object setUserData(String name,Object value);

    /**
     * Removes the name/value registry with the specified name.
     *
     * @param name the name to look for.
     * @return the associated value or null if no registry has this name.
     */
    public Object removeUserData(String name);
    /**
     * Removes the name/value registry with the specified name.
     *
     * @param name the name to look for.
     * @param returnType expected data type.
     * @return the associated value or null if no registry has this name.
     */
    public <T> T removeUserData(String name,Class<T> returnType);
}

