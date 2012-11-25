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

package jepl.impl.jta;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import jepl.impl.JEPLConnectionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLJTADataSourceForUserTxnJDBCImpl extends JEPLJTADataSourceImpl
{

    public JEPLJTADataSourceForUserTxnJDBCImpl(JEPLBootJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
    }

    public UserTransactionJDBC getUserTransactionJDBC()
    {
        return (UserTransactionJDBC)getUserTransaction();
    }

    @Override
    protected JEPLConnectionImpl getJEPLConnectionFromPoolEffective() throws SQLException
    {
        JEPLConnectionImpl jcon = super.getJEPLConnectionFromPoolEffective();
        Connection con = jcon.getConnection();
        getUserTransactionJDBC().addConnection(con);
        return jcon;
    }

    @Override
    protected void returnJEPLConnectionToPoolEffective(JEPLConnectionImpl jcon) throws SQLException
    {
        Connection con = jcon.getConnection();
        getUserTransactionJDBC().removeConnection(con);

        // Hacemos igual que en non-JTA
        if (!con.getAutoCommit())
            con.setAutoCommit(true); // Incluso en el caso de transacción finalizada es conveniente

        super.returnJEPLConnectionToPoolEffective(jcon);
    }
}
