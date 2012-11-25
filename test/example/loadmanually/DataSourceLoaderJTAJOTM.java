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

import example.DataSourceLoaderJTA;
import javax.sql.DataSource;


import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.objectweb.jotm.Current;

import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;


/**
 *
 * @author jmarranz
 */
public class DataSourceLoaderJTAJOTM extends DataSourceLoaderManualLoad implements DataSourceLoaderJTA
{
    protected static TMService jotm = null;
    protected static Current ut;
    static
    {
        try
        {
            jotm = new Jotm(true, false);

            ut = Current.getCurrent();   // org.objectweb.jotm.UserTransactionFactory factory;

            if (ut == null)
            {
                ut = new Current();
                // ut.setDefaultTimeout(timeout);
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    //protected StandardXADataSource ds;
    protected StandardXAPoolDataSource xads;

    public DataSourceLoaderJTAJOTM(String configFile)
    {
        super(configFile);
        
        try
        {
            int maxSize = Integer.parseInt(props.getProperty("xapool.maxSize"));
            int minSize = Integer.parseInt(props.getProperty("xapool.minSize"));

            StandardXADataSource ds = new StandardXADataSource();

            ds.setDriverName(jdbcDriver);
            ds.setUrl(url);
            ds.setUser(userName);
            ds.setPassword(password);
            ds.setPreparedStmtCacheSize(0); // PreparedStatement cache seems buggy (shared between connections?), disabled
            
            //ds.setMaxCon(maxSize);
            //ds.setMinCon(minSize);
            ds.setTransactionManager(jotm.getTransactionManager());

            this.xads = new StandardXAPoolDataSource(ds);
            xads.setUser(userName); 
            xads.setPassword(password); 
            xads.setMaxSize(maxSize);
            xads.setMinSize(minSize);
            
            // xads.setJdbcTestStmt(value);
            // xads.setCheckLevelObject(max);
            // xads.setDeadLockMaxWait(max);
            xads.setTransactionManager(jotm.getTransactionManager());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName()
    {
        return "JOTM";
    }

    @Override
    public DataSource getDataSource()
    {
        //return ds;
        return xads;
    }

    @Override
    public void destroy()
    {
        xads.shutdown(true);
    }

    @Override
    public UserTransaction getUserTransaction()
    {
        return ut;
    }
    
    @Override
    public TransactionManager getTransactionManager()
    {
        return ut;
    }
}
