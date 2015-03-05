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
package unittest.shared;

import java.sql.SQLException;

import example.loadmanually.DataSourceLoaderManualLoad;
import jepl.JEPLConnection;
import jepl.JEPLTask;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jmarranz
 */
public class TestContactDAOSharedNonJTA extends TestContactDAOShared
{
    protected void testSetupJEPLConnectionCommit(JEPLConnection con,JEPLTask<Integer> task,boolean[] used) throws Exception
    {
        con.getConnection().setAutoCommit(false); // Transaction
        try
        {
            int res = task.exec();
            if (!DataSourceLoaderManualLoad.android) 
            	assertTrue(res > 0);
            con.getConnection().commit();
            used[0] = true;
        }
        catch(SQLException ex)
        {
            con.getConnection().rollback();
            throw ex;
        }
    }
    
    protected void testSetupJEPLConnectionForcedRollback(JEPLConnection con,JEPLTask<Integer> task,boolean[] used) throws Exception
    {    
        con.getConnection().setAutoCommit(false); // Transaction
        try
        {
            int res = task.exec();
            if (!DataSourceLoaderManualLoad.android) 
            	assertTrue(res > 0);
            throw new SQLException("Forcing a rollback (not commited)");
        }
        catch(SQLException ex)
        {
            con.getConnection().rollback();
            used[0] = true;
            throw ex;
        }    
    }
}
