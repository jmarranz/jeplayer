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
import scaling.shared.PersonDAOScaling;
import example.model.Person;
import java.util.List;
import java.util.Random;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLException;
import jepl.JEPLJTADataSource;
import jepl.JEPLJTAMultipleDataSource;
import jepl.JEPLTask;
import jepl.JEPLTransactionPropagation;
import jepl.JEPLTransactionalJTA;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import scaling.shared.TestScalingConf;
import scaling.shared.TestScalingJTAShared;

/**
 *
 * @author jmarranz
 */
public class TestScalingJTAUpdateConcurrent
{

    public TestScalingJTAUpdateConcurrent()
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

        PersonDAOScaling[] personDaoArr = new PersonDAOScaling[dsArr.length];
        for(int i = 0; i < dsArr.length ; i++)
            personDaoArr[i] = new PersonDAOScaling(jdsArr[i]);

        int numberOfTestRepetitions = conf.getNumberOfTestRepetitions();

        for(int i = 0; i < numberOfTestRepetitions; i++)
            test(conf,jdsMgr,personDaoArr);
    }

    public void test(final TestScalingConf conf,final JEPLJTAMultipleDataSource jdsMgr,final PersonDAOScaling[] personDaoArr) throws Exception
    {
        final int[] updated = new int[1];

        int numberOfThreads = conf.getNumberOfThreads();

        Random randRoot = new Random();
        final Random[] randArr = new Random[numberOfThreads];
        for(int i = 0; i < numberOfThreads; i++)
            randArr[i] = new Random(randRoot.nextLong()); // Para evitar el uso de un solo Random bloqueante

        Thread[] threadArray = new Thread[numberOfThreads];

        final boolean[] run = new boolean[]{false};
        for(int i = 0; i < threadArray.length; i++)
        {
            final int threadNumber = i;
            Thread thread = new Thread()
            {
                @Override
                public void run()
                {
                    while(!run[0]) Thread.yield();
                    try
                    {
                        executeActionsByThread(conf,jdsMgr, personDaoArr, randArr[threadNumber],updated);
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            };
            thread.start();
            threadArray[i] = thread;
        }

        long start = System.currentTimeMillis();

        run[0] = true;

        for(int i = 0; i < threadArray.length; i++)
            threadArray[i].join();

        long end = System.currentTimeMillis();
        long lapse = end - start;
        System.out.println("LAPSE: " + lapse);
        System.out.println("UPDATED: " + updated[0] + ", per second: " + (1000.0*updated[0]/lapse));
    }

    public void executeActionsByThread(TestScalingConf conf,final JEPLJTAMultipleDataSource jdsMgr,final PersonDAOScaling[] personDaoArr,
            final Random rand, final int[] updated) throws Exception
    {
        int loopsPerRepetition = conf.getNumberOfLoopsEveryRepetition();
        final int masterDataSourceIndex = TestScalingJTAShared.getMasterDataSourceIndex(conf,personDaoArr);
        final int closerDataSourceIndex = TestScalingJTAShared.getCloserDataSourceIndex(conf,personDaoArr);

        final boolean testRollback = conf.getTestRollback();

        for(int loop = 0; loop < loopsPerRepetition; loop++)
        {
            PersonDAOScaling dao = personDaoArr[closerDataSourceIndex];
            List<Person> list = dao.selectRangeOrderByIdDesc(0,1);
            final Person person = list.get(0); // Ever the last one inserted (ever the same but updated)
            JEPLTask task = new JEPLTask()
            {
                @JEPLTransactionalJTA(propagation=JEPLTransactionPropagation.REQUIRED)
                public Object exec() throws Exception
                {
                    TestScalingJTAShared.updatePerson(masterDataSourceIndex,person,personDaoArr,testRollback,rand);
                    updated[0]++;
                    if (updated[0] % 30 == 0)
                        System.out.println("Updated: age:" + person.getAge() + " - count: " + updated[0]);
                    return null;
                }
            };
            try
            {
                jdsMgr.exec(task);
            }
            catch(JEPLException ex)
            {
                if (ex.getCause() == null || !ex.getCause().getMessage().startsWith("FALSE ERROR"))
                    throw new RuntimeException("Unexpected",ex);
                else
                    System.out.println("EXPECTED ROLLBACK (UPDATE)");
            }
        }
    }
}
