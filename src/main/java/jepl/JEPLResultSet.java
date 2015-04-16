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

import java.sql.ResultSet;

/**
 * This interface is a wrapper of a standard JDBC ResultSet.
 *
 * <p>The mission of this interface (JEPLayer provides an implementation) is to simplify the use of the underline java.sql.ResultSet, for instance 
 * when the iteration ends ResultSet is automatically closed.</p>
 * 
 * @see JEPLResultSetDALListener
 * @author jmarranz
 */
public interface JEPLResultSet extends JEPLUserData
{
    /**
     * Returns the parent statement wrapper.
     *
     * @return the parent statement wrapper.
     */  
    public JEPLStatement getJEPLStatement();

    /**
     * Returns the wrapped ResultSet.
     *
     * @return the wrapped ResultSet.
     */
    public ResultSet getResultSet();

    /**
     * Moves the cursor forward one row from its current position using the underlying ResultSet.
     *
     * @return false if there is no more rows.
     */
    public boolean next();    
    
    /**
     * When iterating the result set, this method instructs JEPLayer to stop the iteration.
     */
    public void stop();
    
    /**
     * Informs whether this result set is stopped (iteration has ended).
     * 
     * @return true if this result set is stopped.
     */
    public boolean isStopped();

    /**
     * Closes this result set, the underlying JDBC ResultSet is also closed. Use only
     * this method in a {@link JEPLResultSetDAO} instance.
     */
    public void close();

    /**
     * Informs whether this result set is closed.
     *
     * @return true if this result set is closed.
     */
    public boolean isClosed();
    
    /**
     * Returns the total number of rows returned.
     *
     * <p>If you call this method and the ResultSet is still alive, the ResultSet is iterated
     * to the end and closed.
     * </p>
     * 
     * @return the total number of rows.
     */
    public int count();    
}
