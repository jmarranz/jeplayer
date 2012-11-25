/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

/**
 *
 * @author jmarranz
 */
public class DataSourceFactoryOfLoaderManuallyShared 
{
    public final static String jdbcConfigFileName = "databaseconf";
    public final static String jdbcConfigFileExt = ".properties";
    
    protected static String getFirstConfigFileName()
    {
        return getConfigFileName(1);
    }

    protected static String getConfigFileName(int dataSourceIndex)
    {
        return jdbcConfigFileName + "_" + dataSourceIndex + jdbcConfigFileExt;
    }    
}
