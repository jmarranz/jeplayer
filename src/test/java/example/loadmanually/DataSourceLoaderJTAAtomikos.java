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

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import example.DataSourceLoaderJTA;
import java.util.Properties;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 *
 * @author jmarranz
 */
public class DataSourceLoaderJTAAtomikos extends DataSourceLoaderManualLoad implements DataSourceLoaderJTA
{
    public final static UserTransactionManager utm = new UserTransactionManager();
    public static int counter = 0;
    static
    {
        try
        {
            utm.init();
        }
        catch (SystemException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    protected AtomikosDataSourceBean ds;

    public DataSourceLoaderJTAAtomikos(String configFile)
    {
        super(configFile);

        counter++;

        int maxSize = Integer.parseInt(props.getProperty("atomikos.maxSize"));
        int minSize = Integer.parseInt(props.getProperty("atomikos.minSize"));

        String jdbcXADriver = props.getProperty("jdbc.xa.driver");
        
        this.ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("atomikos_datasource_" + counter);
        ds.setXaDataSourceClassName(jdbcXADriver);
        Properties p = new Properties();
        p.setProperty( "user" , userName );
        p.setProperty( "password" , password );
        p.setProperty( "URL" , url );
        ds.setXaProperties(p);
        //ds.setPoolSize ( 5 );
        ds.setMaxPoolSize(maxSize);
        ds.setMinPoolSize(minSize);
    }

    public String getName()
    {
        return "Atomikos";
    }

    @Override
    public DataSource getDataSource()
    {
        return ds;
    }

    @Override
    public void destroy()
    {
        ds.close();
    }

    @Override
    public UserTransaction getUserTransaction()
    {
        return utm;
    }
    
    @Override
    public TransactionManager getTransactionManager()
    {
        return utm;
    }
}
