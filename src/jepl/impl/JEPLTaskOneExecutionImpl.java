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

import jepl.JEPLException;
import jepl.JEPLTask;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLTaskOneExecutionImpl<T> implements JEPLTask<T>
{
    protected T result;
    protected boolean executed = false;

    public JEPLTaskOneExecutionImpl()
    {
    }

    public T getResult()
    {
        return result;
    }

    public boolean isExecuted()
    {
        return executed;
    }

    public T exec() throws Exception
    {
        if (executed) throw new JEPLException("Task is already executed");
        this.executed = true;

        this.result = execInherit();
        return result;
    }
    
    protected abstract T execInherit() throws Exception;
}
