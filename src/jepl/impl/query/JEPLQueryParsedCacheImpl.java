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

package jepl.impl.query;

import java.util.LinkedHashMap;
import jepl.JEPLException;
import jepl.impl.JEPLBootImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLQueryParsedCacheImpl
{
    protected ThreadLocal<LinkedHashMap<String,JEPLQueryParsedImpl>> cachedQueriesByThread = new ThreadLocal<LinkedHashMap<String,JEPLQueryParsedImpl>>();
    protected int maxCached = 500; // hay que tener en cuenta que son 500 consultas cacheadas por hilo que no está nada mal
    protected JEPLBootImpl boot;
    
    public JEPLQueryParsedCacheImpl(JEPLBootImpl boot)
    {
        this.boot = boot;
    }

    public int getMaxCachedPerThread()
    {
        return maxCached;
    }

    public void setMaxCachedPerThread(int maxCached)
    {
        if (boot.isInUse()) throw new JEPLException("Cannot be changed because some query has been executed");
        this.maxCached = maxCached;
    }

    public JEPLQueryParsedImpl getJEPLQueryParsed(String sqlOriginal)
    {
        if (maxCached <= 0)
        {
            // Caché desactivada
            return new JEPLQueryParsedImpl(sqlOriginal);
        }
        else
        {
            // Como el caché es por hilo no necesitamos sincronizar y así no provocamos bloqueos
            LinkedHashMap<String,JEPLQueryParsedImpl> cachedQueriesOfThread = cachedQueriesByThread.get();
            if (cachedQueriesOfThread == null)
            {
                cachedQueriesOfThread = new LinkedHashMap<String,JEPLQueryParsedImpl>();
                cachedQueriesByThread.set(cachedQueriesOfThread);
            }
            JEPLQueryParsedImpl query = cachedQueriesOfThread.get(sqlOriginal);
            if (query == null)
            {
                query = new JEPLQueryParsedImpl(sqlOriginal);
                if (cachedQueriesOfThread.size() == maxCached)
                {
                    // Esto lo hacemos para que no crezca indefinidamente el Map caché, esto podría ocurrir
                    // si el programador crea consultas SQL a pelo con valores de claves.
                    // Eliminamos el primer valor memorizado (el más antiguo)
                    String key = cachedQueriesOfThread.keySet().iterator().next();
                    cachedQueriesOfThread.remove(key);
                }

                cachedQueriesOfThread.put(sqlOriginal, query);
            }
            return query;
        }
    }

}
