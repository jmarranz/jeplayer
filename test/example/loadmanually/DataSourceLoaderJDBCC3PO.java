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
package example.loadmanually;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author jmarranz
 */
public class DataSourceLoaderJDBCC3PO extends DataSourceLoaderManualLoad
{
    protected ComboPooledDataSource cpds;

    public DataSourceLoaderJDBCC3PO(String configFile)
    {
        super(configFile);
        
        try
        {
            int poolSize = Integer.parseInt(props.getProperty("c3po.poolSize"));
            int maxStatements = Integer.parseInt(props.getProperty("c3po.maxStatements"));

            // http://www.codecommit.com/blog/java/wide-world-of-pool-providers-side-by-side-comparison

            this.cpds = new ComboPooledDataSource();
            cpds.setDriverClass(jdbcDriver);

            cpds.setJdbcUrl(url);
            cpds.setUser(userName);
            cpds.setPassword(password);

            cpds.setMaxPoolSize(poolSize);
            cpds.setMaxStatements(maxStatements);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getName()
    {
        return "JDBCC3PO";
    }

    @Override
    public DataSource getDataSource()
    {
        return cpds;
    }

    @Override
    public void destroy()
    {
        try
        {
            DataSources.destroy(cpds);
            cpds.close();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
