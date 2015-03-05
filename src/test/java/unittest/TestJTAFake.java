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
package unittest;

import example.CreateDBModel;
import example.DataSourceFactoryOfLoaderJTA;
import unittest.shared.TestJTAShared;
import example.DataSourceLoaderJTA;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;

/**
 *
 * @author jmarranz
 */
public class TestJTAFake
{
    public TestJTAFake()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void someTest() throws Exception
    {
        DataSourceFactoryOfLoaderJTA dsLoader = DataSourceFactoryOfLoaderJTA.getDataSourceFactoryOfLoaderJTA();

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_JTAJNDI))
            execTest(dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_JTAJNDI);

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_JOTM))
            execTest(dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_JOTM);

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_ATOMIKOS))
            execTest(dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_ATOMIKOS);
    }

    public void execTest(DataSourceFactoryOfLoaderJTA dsLoader,String jtaProvider)
    {
        System.out.println("PROVIDER: " + jtaProvider);

        DataSourceLoaderJTA dsJTAFactory = dsLoader.createDataSourceLoaderJTA(jtaProvider);
        
        CreateDBModel.createDB(dsJTAFactory.getDataSource());      
        
        try
        {
            TestJTAShared.execTest(dsJTAFactory,true,jtaProvider); // Fake transaction
        }
        finally
        {
            dsJTAFactory.destroy();
        }
    }
}
