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


/**
 *
 * @author jmarranz
 */
public class DataSourceFactoryOfLoaderJDBCJNDI extends DataSourceFactoryOfLoaderJDBC
{
    public DataSourceFactoryOfLoaderJDBCJNDI()
    {
    }
   
    public DataSourceLoader[] getDataSourceLoaderList()
    {
        return new DataSourceLoader[] { new DataSourceLoaderJTAJNDI(DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME) };
    }
    
    public DataSourceLoader createDataSourceLoaderJDBC()
    {
        return new DataSourceLoaderJTAJNDI(DataSourceFactoryOfLoaderJNDIShared.JNDI_NAME);
    }

}
