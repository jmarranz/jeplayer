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

package jepl.impl.jta.dsmgr;

import java.util.LinkedList;

/**
 *
 * @author jmarranz
 */
public class JEPLTaskListSameThreadImpl
{
    protected LinkedList<JEPLTaskExecContextInJTADataSourceMgrImpl<?>> taskList = new LinkedList<JEPLTaskExecContextInJTADataSourceMgrImpl<?>>();
    
    public boolean isEmptyOfJEPLTasks()
    {
        return taskList.isEmpty();
    }

    public JEPLTaskExecContextInJTADataSourceMgrImpl<?> getCurrentJEPLTaskContext()
    {
        return taskList.getFirst(); // El último insertado realmente es el primero de la lista (se usó push)
    }

    public JEPLTaskExecContextInJTADataSourceMgrImpl<?> popJEPLTaskExecContex()
    {
        return taskList.pop(); // Quita del ppio
    }

    public void pushJEPLTaskExecContex(JEPLTaskExecContextInJTADataSourceMgrImpl<?> task)
    {
        taskList.push(task); // Pone en el ppio
    }
}
