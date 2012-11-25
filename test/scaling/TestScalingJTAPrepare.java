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
package scaling;

import example.CreateDBModel;
import example.DataSourceFactoryOfLoaderJTA;
import example.DataSourceLoaderJTA;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLJTADataSource;
import jepl.JEPLJTAMultipleDataSource;
import org.junit.*;
import scaling.shared.PersonDAOScaling;
import scaling.shared.TestScalingConf;
import scaling.shared.TestScalingJTAShared;

/**
 *
 * @author jmarranz
 */
public class TestScalingJTAPrepare
{

    public TestScalingJTAPrepare()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception  {  }

    @AfterClass
    public static void tearDownClass() throws Exception  {  }

    @Before
    public void setUp() {  }

    @After
    public void tearDown()  {  }

    @Test
    public void someTest() throws Exception
    {
        TestScalingConf conf = new TestScalingConf();

        DataSourceFactoryOfLoaderJTA dsLoader = DataSourceFactoryOfLoaderJTA.getDataSourceFactoryOfLoaderJTA();
        String provider = TestScalingJTAShared.getProvider(conf,dsLoader);

        execTest(conf,dsLoader,provider);

        System.out.println("END TEST");
    }

    public void execTest(TestScalingConf conf, DataSourceFactoryOfLoaderJTA dsLoader,String jtaProvider) throws Exception
    {
        System.out.println("PROVIDER: " + jtaProvider);

        DataSourceLoaderJTA[] dsFactoryArr = TestScalingJTAShared.createDataSourceLoaderJTAs(conf,dsLoader,jtaProvider);

        DataSource[] dsArr = new DataSource[dsFactoryArr.length];
        for(int i = 0; i < dsFactoryArr.length; i++)
        {
            DataSource ds = dsFactoryArr[i].getDataSource();
            dsArr[i] = ds;
        }

        UserTransaction txn = dsFactoryArr[0].getUserTransaction();
        TransactionManager txnMgr = dsFactoryArr[0].getTransactionManager();

        try
        {
            test(conf,dsArr,txn,txnMgr);
        }
        finally
        {
            for(int i = 0; i < dsFactoryArr.length; i++)
                dsFactoryArr[i].destroy();
        }
    }

    public void test(TestScalingConf conf,DataSource[] dsArr,UserTransaction txn,TransactionManager txnMgr) throws Exception
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        boot.setUserTransaction(txn);
        boot.setTransactionManager(txnMgr);

        JEPLJTAMultipleDataSource jdsMgr = boot.getJEPLJTAMultipleDataSource();
        JEPLJTADataSource[] jdsArr = TestScalingJTAShared.getJEPLJTADataSourceList(dsArr,jdsMgr);

        final PersonDAOScaling[] personDaoArr = new PersonDAOScaling[dsArr.length];
        for(int i = 0; i < dsArr.length ; i++)        
            personDaoArr[i] = new PersonDAOScaling(jdsArr[i]);

        test(conf,dsArr,personDaoArr);
    }

    public void test(TestScalingConf conf,DataSource[] dsArr,PersonDAOScaling[] personDaoArr) throws Exception
    {
        for(int i = 0; i < dsArr.length; i++)
        {
            CreateDBModel.createDB(dsArr[i]);
        }

        int masterDataSourceIndex = TestScalingJTAShared.getMasterDataSourceIndex(conf,personDaoArr);
        
        // Insert some initial data
        for(int i = 0; i < 50; i++)
        {
            TestScalingJTAShared.insertPerson(masterDataSourceIndex,personDaoArr,false,null);
        }
    }
}
