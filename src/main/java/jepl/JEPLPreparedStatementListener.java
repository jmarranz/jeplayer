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
 * the JDBC PreparedStatement inmediately before executing it.
 *
 * @author jmarranz
 */
public interface JEPLPreparedStatementListener<T> extends JEPLListener
{
    /**
     * This method is called before processing SQL statements to configure the JDBC PreparedStatement.
     *
     * @param stmt the PreparedStatement wrapper.
     * @param task represents the consecutive task to be executed.
     * @throws Exception
     */    
    public void setupJEPLPreparedStatement(JEPLPreparedStatement stmt,JEPLTask<T> task) throws Exception;
}
