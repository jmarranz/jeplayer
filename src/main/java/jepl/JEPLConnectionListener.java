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
 * If an implementation of this interface is correctly registered, is used to setup
 * the JDBC Connection before processing SQL statements.
 *
 * @param <T> the expected type.  
 * @author jmarranz
 */
public interface JEPLConnectionListener<T> extends JEPLListener
{
    /**
     * This method is called before processing SQL statements to configure the JDBC Connection.
     *
     * @param jcon the connection wrapper.
     * @param task represents the consecutive task to be executed.
     * @throws Exception
     */
    public void setupJEPLConnection(JEPLConnection jcon,JEPLTask<T> task) throws Exception;
}
