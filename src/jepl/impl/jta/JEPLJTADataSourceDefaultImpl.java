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
import javax.sql.DataSource;
import jepl.impl.JEPLConnectionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLJTADataSourceDefaultImpl extends JEPLJTADataSourceImpl
{
    protected boolean useXAPool = false;
    
    public JEPLJTADataSourceDefaultImpl(JEPLBootJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
        
        String className = ds.getClass().getName();
        if (className.equals("org.enhydra.jdbc.pool.StandardXAPoolDataSource") ||
            className.equals("org.enhydra.jdbc.standard.StandardXADataSource")) // Este segundo caso es "por si acaso" pero realmente no está contemplado
            useXAPool = true;
    }

    public boolean isUseXAPool()
    {
        return useXAPool;
    }
    
    @Override
    public JEPLConnectionImpl createJEPLConnection(Connection con)
    {
        if (useXAPool)
            return new JEPLJTAConnectionXAPoolImpl(this,con);
        else
            return super.createJEPLConnection(con);
    }
}
