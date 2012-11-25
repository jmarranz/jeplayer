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

package example;

import example.jndi.DataSourceLoaderJTAJNDI;
import javax.naming.Context;
import javax.naming.InitialContext;


/**
 *
 * @author jmarranz
 */
public class DataSourceFactoryOfLoaderJTAJNDI extends DataSourceFactoryOfLoaderJTA
{
    public DataSourceFactoryOfLoaderJTAJNDI()
    {
    }

    public static boolean useJTAJNDI()
    {
        boolean useJTAJNDI;
        try
        {
            Context ctx = new InitialContext();
            useJTAJNDI = (ctx.lookup( DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME ) != null);
        }
        catch(Exception ex)
        {
            useJTAJNDI = false;
        }
        
        return useJTAJNDI;        
    }
    
    public DataSourceLoaderJTA createDataSourceLoaderJTA(String jtaProvider)
    {
        return new DataSourceLoaderJTAJNDI(DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME);
    }

    public boolean isJTAProviderSupported(String jtaProvider)
    {
        return PROVIDER_JTAJNDI.equals(jtaProvider);
    }


    public DataSourceLoaderJTA getDataSourceLoaderJTA(int dataSourceIndex,String jtaProvider)
    {
        String jndiName;
        if (dataSourceIndex == 1) jndiName = DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME;
        else jndiName = DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME + "_" + dataSourceIndex;
        return new DataSourceLoaderJTAJNDI(jndiName);
    }
}
