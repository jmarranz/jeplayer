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

import example.loadmanually.DataSourceLoaderJTAAtomikos;
import example.loadmanually.DataSourceLoaderJTAJOTM;

/**
 *
 * @author jmarranz
 */
public class DataSourceFactoryOfLoaderManuallyJTA extends DataSourceFactoryOfLoaderJTA
{
    public DataSourceFactoryOfLoaderManuallyJTA()
    {
    }

    public boolean isJTAProviderSupported(String jtaProvider)
    {
        return PROVIDER_JOTM.equals(jtaProvider) || PROVIDER_ATOMIKOS.equals(jtaProvider);
    }

    public DataSourceLoaderJTA createDataSourceLoaderJTA(String jtaProvider)
    {
        if (PROVIDER_JOTM.equals(jtaProvider))
            return new DataSourceLoaderJTAJOTM(DataSourceFactoryOfLoaderManuallyShared.getFirstConfigFileName());
        else if (PROVIDER_ATOMIKOS.equals(jtaProvider))
            return new DataSourceLoaderJTAAtomikos(DataSourceFactoryOfLoaderManuallyShared.getFirstConfigFileName());
        else
            return null;
    }

    public DataSourceLoaderJTA getDataSourceLoaderJTA(int dataSourceIndex,String jtaProvider)
    {
        String currConfigFile = DataSourceFactoryOfLoaderManuallyShared.getConfigFileName(dataSourceIndex);

        if (PROVIDER_JOTM.equals(jtaProvider))
            return new DataSourceLoaderJTAJOTM(currConfigFile);
        else if (PROVIDER_ATOMIKOS.equals(jtaProvider))
            return new DataSourceLoaderJTAAtomikos(currConfigFile);
        else
            return null;
    }
}
