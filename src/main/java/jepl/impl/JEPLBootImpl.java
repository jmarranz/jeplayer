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

import jepl.JEPLBoot;
import jepl.JEPLBootRoot;
import jepl.impl.query.JEPLQueryParsedCacheImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLBootImpl implements JEPLBoot
{
    protected JEPLUserDataMultiThreadImpl userData = new JEPLUserDataMultiThreadImpl();
    protected JEPLBootRootImpl root;
    protected JEPLQueryParsedCacheImpl queryCache = new JEPLQueryParsedCacheImpl(this);
    protected boolean inUse = false;
 
    
    public JEPLBootImpl(JEPLBootRootImpl root)
    {
        this.root = root;
    }

    public JEPLQueryParsedCacheImpl getJEPLQueryParsedCache()
    {
        return queryCache;
    }

    @Override
    public int getMaxParsedQueriesCachedPerThread()
    {
        return queryCache.getMaxCachedPerThread();
    }

    @Override
    public void setMaxParsedQueriesCachedPerThread(int value)
    {
        queryCache.setMaxCachedPerThread(value);
    }
    
    public boolean isInUse()
    {
        return inUse;
    }

    public void setInUse()
    {
        if (!inUse)
            this.inUse = true;
    }

    @Override
    public String[] getUserDataNames()
    {
        return userData.getUserDataNames();
    }

    @Override
    public boolean containsName(String name)
    {
        return userData.containsName(name);
    }

    @Override
    public Object getUserData(String name)
    {
        return userData.getUserData(name);
    }

    @Override
    public <T> T getUserData(String name, Class<T> returnType)
    {
        return userData.getUserData(name, returnType);
    }

    @Override
    public Object setUserData(String name, Object value)
    {
        return userData.setUserData(name, value);
    }

    @Override
    public Object removeUserData(String name)
    {
        return userData.removeUserData(name);
    }

    @Override
    public <T> T removeUserData(String name, Class<T> returnType)
    {
        return userData.removeUserData(name, returnType);
    }

    @Override
    public JEPLBootRoot getJEPLBootRoot()
    {
        return root;
    }

}
