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

import java.sql.Statement;

/**
 * This interface is a wrapper of a standard JDBC Statement.
 *
 * @author jmarranz
 */
public interface JEPLStatement extends JEPLUserData
{
    /**
     * Returns the connection wrapper associated to this statement.
     *
     * @return the connection wrapper of this statement.
     */    
    public JEPLConnection getJEPLConnection();

    /**
     * Returns the DAL object used to execute this statement.
     * 
     * @return the DAL object of this statement.
     */
    public JEPLDAL getJEPLDAL();

    /**
     * Returns the wrapped Statement.
     *
     * @return the wrapped Statement.
     */
    public Statement getStatement();
}
