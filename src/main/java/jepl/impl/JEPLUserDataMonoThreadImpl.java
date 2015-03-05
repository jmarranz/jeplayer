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

package jepl.impl;

import java.util.HashMap;
import java.util.Map;
import jepl.JEPLUserData;

/**
 *
 * @author jmarranz
 */
public class JEPLUserDataMonoThreadImpl implements JEPLUserData
{
    protected Map<String,Object> userData;

    /**
     * Creates a new instance of JEPLUserDataMonoThreadImpl
     */
    public JEPLUserDataMonoThreadImpl()
    {
    }

    public Map<String,Object> getInternalMap()
    {
        if (userData == null) userData = new HashMap<String,Object>();
        return userData;
    }

    public String[] getUserDataNames()
    {
        Map<String,Object> userData = getInternalMap();
        String[] names = new String[userData.size()];
        return userData.keySet().toArray(names);
    }

    public boolean containsName(String name)
    {
        Map<String,Object> userData = getInternalMap();
        return userData.containsKey(name);
    }

    public Object getUserData(String name)
    {
        Map<String,Object> userData = getInternalMap();
        return userData.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getUserData(String name, Class<T> returnType)
    {
        return (T)getUserData(name);
    }

    public Object setUserData(String name,Object value)
    {
        Map<String,Object> userData = getInternalMap();
        return userData.put(name,value);
    }

    public Object removeUserData(String name)
    {
        Map<String,Object> userData = getInternalMap();
        return userData.remove(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T removeUserData(String name, Class<T> returnType)
    {
        return (T)removeUserData(name);
    }

}
