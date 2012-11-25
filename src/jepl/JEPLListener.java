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
 * This is the base interface of listeners designed as hooks to control the lifecycle
 * of the persistent actions.
 *
 * @author jmarranz
 * @see JEPLDataSource#addJEPLListener(JEPLListener)
 * @see JEPLDAL#addJEPLListener(JEPLListener)
 * @see JEPLDALQuery#addJEPLListener(JEPLListener)
 * @see JEPLDataSource#exec(JEPLTask,JEPLListener)
 * @see JEPLJTADataSource#exec(JEPLTask,JEPLListener ,JEPLTransactionPropagation )
 */
public interface JEPLListener
{
}
