/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scaling.shared;

import java.util.Properties;

/**
 *
 * @author jmarranz
 */
public class TestScalingConf
{
    protected int numberOfDataSources;
    protected int onOneDataSourceUse;
    protected int masterDataSource;
    protected int closerDataSource;
    protected int numberOfThreads;
    protected int numberOfTestRepetitions;
    protected int loopsEveryRepetition;
    protected int ratioSelectChange;
    protected int ratioInsertDelete;
    protected boolean testRollback;
    protected String[] providerList;
    
    public TestScalingConf()
    {
        Properties props = loadProperties("databaseconf_global.properties");
        this.numberOfDataSources = Integer.parseInt(props.getProperty("number_of_data_sources"));
        this.onOneDataSourceUse = Integer.parseInt(props.getProperty("on_one_data_source_use"));
        this.masterDataSource = Integer.parseInt(props.getProperty("master_data_source"));
        this.closerDataSource = Integer.parseInt(props.getProperty("closer_data_source"));         
        this.numberOfThreads = Integer.parseInt(props.getProperty("number_of_threads"));
        this.numberOfTestRepetitions = Integer.parseInt(props.getProperty("number_of_test_repetitions"));
        this.loopsEveryRepetition = Integer.parseInt(props.getProperty("loops_every_repetition"));
        this.ratioSelectChange = Integer.parseInt(props.getProperty("ratio_select_change"));
        this.ratioInsertDelete = Integer.parseInt(props.getProperty("ratio_insert_delete"));
        this.testRollback = Boolean.parseBoolean(props.getProperty("test_rollback"));

        String providers = props.getProperty("providerJTA");
        this.providerList = providers.split(",");
        for(int i = 0; i < providerList.length; i++)
            providerList[i] = providerList[i].trim();
    }

    public int getCloserDataSource()
    {
        return closerDataSource;
    }

    public static Properties loadProperties(String fileName)
    {
        Properties props = new Properties();
        try
        {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
            return props;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public int getLoopsEveryRepetition()
    {
        return loopsEveryRepetition;
    }

    public int getMasterDataSource()
    {
        return masterDataSource;
    }

    public boolean isTestRollback()
    {
        return testRollback;
    }


    public int getNumberOfDataSources()
    {
        return numberOfDataSources;
    }

    public int getOnOneDataSourceUse()
    {
        return onOneDataSourceUse;
    }

    public int getNumberOfLoopsEveryRepetition()
    {
        return loopsEveryRepetition;
    }

    public int getNumberOfTestRepetitions()
    {
        return numberOfTestRepetitions;
    }

    public int getNumberOfThreads()
    {
        return numberOfThreads;
    }

    public int getRatioInsertDelete()
    {
        return ratioInsertDelete;
    }

    public int getRatioSelectChange()
    {
        return ratioSelectChange;
    }

    public boolean getTestRollback()
    {
        return testRollback;
    }

    public String[] getProviderList()
    {
        return providerList;
    }
}
