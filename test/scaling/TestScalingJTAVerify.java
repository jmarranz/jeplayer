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

import example.DataSourceFactoryOfLoaderJTA;
import example.DataSourceLoaderJTA;
import example.model.Person;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.*;
import org.junit.*;
import scaling.shared.PersonDAOScaling;
import scaling.shared.TestScalingConf;
import scaling.shared.TestScalingJTAShared;

/**
 *
 * @author jmarranz
 */
public class TestScalingJTAVerify
{

    public TestScalingJTAVerify()
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

    public void execTest(TestScalingConf conf,DataSourceFactoryOfLoaderJTA dsLoader,String jtaProvider) throws Exception
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

        boolean res = checkSameContent(jdsMgr,personDaoArr);
        if (res)
            System.out.println("DATABASES ARE EQUAL");
        else
            System.out.println("SOMETHING IS WRONG DBs ARE DIFFERENT!!!");
    }

    public boolean checkSameContent(JEPLJTAMultipleDataSource jdsMgr,final PersonDAOScaling[] personDaoArr)
    {
        final boolean[] res = new boolean[]{true};

        JEPLTask task = new JEPLTask()
        {
            @JEPLTransactionalJTA
            public Object exec() throws Exception
            {
                List[] arrayOfList = new List[personDaoArr.length];
                for(int i = 0; i < personDaoArr.length ; i++)
                {
                    PersonDAOScaling currDao = personDaoArr[i];
                    List<Person> list = currDao.selectAllOrderById();
                    arrayOfList[i] = list;
                }

                List<Person> list = arrayOfList[0];
                System.out.println("ROWS IN DB:" + list.size());
                for(int i = 0; i < list.size(); i++)
                {
                    Person personRef = list.get(i);
                    for(int j = 1; j < personDaoArr.length ; j++)
                    {
                        List<Person> listToCompare = arrayOfList[j];
                        if (list.size() != listToCompare.size())
                        {
                            res[0] = false;
                            break;
                        }
                        Person personToCompare = listToCompare.get(i);
                        if (personRef.getId() != personToCompare.getId())
                        {
                            res[0] = false;
                            break;
                        }
                    }
                }

                return null;
            }
        };
        jdsMgr.exec(task);

        return res[0];
    }

}
