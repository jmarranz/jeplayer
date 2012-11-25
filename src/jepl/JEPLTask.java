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
 * This interface is used internally by the library and by end user code to define a persistent task
 * or the next next of the persistent action lifecycle.
 *
 * @author jmarranz
 */
public interface JEPLTask<T>
{
    /**
     * This method is called to execute the task, the returned value depends on the task to execute.
     *
     * <p>Implementations of this method by end users can hold the {@link JEPLTransactionalJTA}
     * annotation to describe JTA transactions for JTA data sources and {@link JEPLTransactionalNonJTA} in basic JDBC
     * transactions of non-JTA data sources.</p>
     *
     * @return the returned value of the task.
     * @throws Exception
     */
    public T exec() throws Exception;
}
