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

import java.sql.ResultSet;
import java.sql.SQLException;
import jepl.impl.JEPLPreparedStatementImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLResultSetDefaultImpl extends JEPLResultSetImpl
{
    protected boolean stopped = false;
    
    public JEPLResultSetDefaultImpl(JEPLDALQueryImpl query,JEPLPreparedStatementImpl stmt,ResultSet result) throws SQLException
    {
        super(query,stmt,result);
    }
    
    @Override
    public boolean isStopped()
    {
        return stopped;
    }

    @Override
    public void stop()
    {
        this.stopped = true;
    }

    @Override
    public String getErrorMsgClosed() {
        return "This result set is already closed";
    }

    @Override
    public Object getRowContent() {
        // Nada, es útil sólo para DAO
        return null;
    }
}
