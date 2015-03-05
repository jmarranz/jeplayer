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

import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author jmarranz
 */
public class DataSourceLoaderSimpleDS extends DataSourceLoaderManualLoad
{
    protected SimpleDataSource simpleDS;

    public DataSourceLoaderSimpleDS(String configFile)
    {
        super(configFile);
        
        int poolSize = Integer.parseInt(props.getProperty("simpleDS.poolSize"));            
            
        this.simpleDS = new SimpleDataSource(jdbcDriver,url,userName,password,poolSize);
    }

    public String getName()
    {
        return "SimpleDS";
    }

    @Override
    public DataSource getDataSource()
    {
        return simpleDS;
    }

    @Override
    public void destroy()
    {
        try
        {
            simpleDS.destroy();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
