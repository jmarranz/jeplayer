/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example;

/**
 *
 * @author jmarranz
 */
public abstract class DataSourceFactoryOfLoaderJTA
{
    public final static String PROVIDER_JOTM = "JOTM";
    public final static String PROVIDER_ATOMIKOS = "Atomikos";
    public final static String PROVIDER_JTAJNDI = "JTAJNDI";

    public static DataSourceFactoryOfLoaderJTA getDataSourceFactoryOfLoaderJTA()
    {
        if (DataSourceFactoryOfLoaderJTAJNDI.useJTAJNDI())
            return new DataSourceFactoryOfLoaderJTAJNDI();
        else
            return new DataSourceFactoryOfLoaderManuallyJTA();
    }

    public DataSourceLoaderJTA[] getDataSourceLoaderListJTA(int numOfDataSources,String jtaProvider)
    {
        DataSourceLoaderJTA[] list = new DataSourceLoaderJTA[numOfDataSources];

        for(int i = 0; i < numOfDataSources; i++)
            list[i] = getDataSourceLoaderJTA( i + 1 , jtaProvider);

        return list;
    }

    public abstract boolean isJTAProviderSupported(String jtaProvider);

    public abstract DataSourceLoaderJTA createDataSourceLoaderJTA(String jtaProvider);

    public abstract DataSourceLoaderJTA getDataSourceLoaderJTA(int dataSourceIndex,String jtaProvider);
}
