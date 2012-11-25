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

import example.DataSourceFactoryOfLoaderJTA;
import example.DataSourceLoaderJTA;
import example.dao.ContactDAO;
import example.model.Contact;
import java.sql.Connection;
import java.util.List;
import jepl.*;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import unittest.shared.TestContactDAOShared;
import unittest.shared.TestDAOShared;

/**
 *
 * @author jmarranz
 */
public class TestJTAMultipleDataSource
{
    public TestJTAMultipleDataSource()
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
        execTest(false);
        execTest(true);
    }

    public void execTest(boolean useFakeUserTxn)
    {
        DataSourceFactoryOfLoaderJTA dsLoader = DataSourceFactoryOfLoaderJTA.getDataSourceFactoryOfLoaderJTA();

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_JTAJNDI))
            execTest(useFakeUserTxn,dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_JTAJNDI);

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_JOTM))
            execTest(useFakeUserTxn,dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_JOTM);

        if (dsLoader.isJTAProviderSupported(DataSourceFactoryOfLoaderJTA.PROVIDER_ATOMIKOS))
            execTest(useFakeUserTxn,dsLoader,DataSourceFactoryOfLoaderJTA.PROVIDER_ATOMIKOS);
    }

    public void execTest(boolean useFakeUserTxn,DataSourceFactoryOfLoaderJTA dsLoader,String jtaProvider)
    {
        System.out.println("PROVIDER: " + jtaProvider);
        DataSourceLoaderJTA dsJTAFactory1 = null;
        DataSourceLoaderJTA dsJTAFactory2 = null;

        dsJTAFactory1 = dsLoader.createDataSourceLoaderJTA(jtaProvider);
        dsJTAFactory2 = dsLoader.createDataSourceLoaderJTA(jtaProvider);
        try
        {
            execTest(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
        }
        finally
        {
            dsJTAFactory1.destroy();
            dsJTAFactory2.destroy();
        }
    }

    public void execTest(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn)
    {
        try
        {
            testJEPLTaskUseGlobalDefaultTransactionMode(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
            testJEPLTaskGlobalSetDefaultJEPLTransactionPropagation(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
            testJEPLTaskNestedAndTransactionModes(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
            testJEPLTaskAnnotated(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
            testJEPLTaskImplicitRollback(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
            testMultithread(dsJTAFactory1,dsJTAFactory2,useFakeUserTxn);
        }
        catch(AssertionError ex)
        {
            ex.printStackTrace(); // To show the stack fully
            throw ex;
        }
        catch(Exception ex)
        {
            ex.printStackTrace(); // To show the stack fully
            throw new RuntimeException(ex);
        }
    }

    public void testJEPLTaskUseGlobalDefaultTransactionMode(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn)
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);
        JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(dsJTAFactory1.getDataSource());
        JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(dsJTAFactory2.getDataSource());
        
        assertTrue(jdsMgr.getDefaultJEPLTransactionPropagation() == JEPLTransactionPropagation.REQUIRED);

        initData(jds1);
        
        JEPLTask<Contact> task = createJEPLTaskDSMgr(jdsMgr,jds1,jds2,JEPLTransactionPropagation.REQUIRED);
        jdsMgr.exec(task);
    }

    public void testJEPLTaskGlobalSetDefaultJEPLTransactionPropagation(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn)
    {
        // El valor por defecto es JEPLTransactionPropagation.REQUIRED
        // que ya fue testeado por lo que cualquier otro valor es suficiente para testear       
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);

        jdsMgr.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED);

        JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(dsJTAFactory1.getDataSource());
        JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(dsJTAFactory2.getDataSource());

        initData(jds1);

        JEPLTask<Contact> task = createJEPLTaskDSMgr(jdsMgr,jds1,jds2,JEPLTransactionPropagation.NOT_SUPPORTED);
        jdsMgr.exec(task);
    }

    public void testJEPLTaskNestedAndTransactionModes(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn) throws Exception
    {
        // Estudiamos si funcionan todos los modos en tasks anidados

        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        final JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);
        final JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(dsJTAFactory1.getDataSource());
        final JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(dsJTAFactory2.getDataSource());

        JEPLTask<Contact> taskOutside;
        

        // MANDATORY
        initData(jds1);
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                try
                {
                    Connection con = jds1.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(1, jds1,JEPLTransactionPropagation.MANDATORY,con);
                    jds1.exec(task,JEPLTransactionPropagation.MANDATORY);
                    assertTrue(false); // No debe llegar aqui
                }
                catch(JEPLException ex)
                {
                    assertTrue(ex.getMessage().equals("Specified MANDATORY transaction propagation mode and there is no active transaction"));
                }

                try
                {
                    Connection con = jds2.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(2, jds2,JEPLTransactionPropagation.MANDATORY,con);
                    jds2.exec(task,JEPLTransactionPropagation.MANDATORY);
                    assertTrue(false); // No debe llegar aqui
                }
                catch(JEPLException ex)
                {
                    assertTrue(ex.getMessage().equals("Specified MANDATORY transaction propagation mode and there is no active transaction"));
                }

                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);

                return null;
            }
        };
        jdsMgr.exec(taskOutside,JEPLTransactionPropagation.NEVER);


        // REQUIRED
        initData(jds1);
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {

                {
                    Connection con = jds1.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(1, jds1,JEPLTransactionPropagation.REQUIRED,con);
                    jds1.exec(task,JEPLTransactionPropagation.REQUIRED);
                }

                {
                    Connection con = jds2.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(2, jds2,JEPLTransactionPropagation.REQUIRED,con);
                    jds2.exec(task,JEPLTransactionPropagation.REQUIRED);
                }

                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);

                return null;
            }
        };
        jdsMgr.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);

        // No probamos más, es más de lo mismo y ya se testea en TestJTA
    }

    public void testJEPLTaskAnnotated(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn) throws Exception
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        final JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);

        jdsMgr.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED); // Para asegurar que el valor por defecto no es REQUIRED y testear que en el primer test se lee el valor de la anotación

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA // Por defecto es REQUIRED
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jdsMgr.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.REQUIRES_NEW)
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRES_NEW);
                return null;
            }
        };
        jdsMgr.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.NEVER)
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jdsMgr.exec(task,JEPLTransactionPropagation.REQUIRED);  // Ha de ganar a la anotación

    }

    /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
     *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
     */
    public synchronized static void testJEPLTaskImplicitRollback(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn) throws Exception
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);
        final JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(dsJTAFactory1.getDataSource());
        final JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(dsJTAFactory2.getDataSource());

        initData(jds1);

        final Contact[] inserted = new Contact[2];
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                Contact[] insertedTmp = insertTwo(jds1,jds2);
                inserted[0] = insertedTmp[0];
                inserted[1] = insertedTmp[1];

                if (true) throw new Exception("SOME EXCEPTION");

                return null;
            }
        };
        try
        {
            jdsMgr.exec(task,JEPLTransactionPropagation.REQUIRED);
            assertTrue(false); // No debe llegar aquí
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getCause().getMessage().equals("SOME EXCEPTION"));
            TestContactDAOShared.testIsRollbacked(jds1,inserted[0]);
            TestContactDAOShared.testIsRollbacked(jds2,inserted[1]);
        }
    }

    public void testMultithread(DataSourceLoaderJTA dsJTAFactory1,DataSourceLoaderJTA dsJTAFactory2,boolean useFakeUserTxn) throws Exception
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        final JEPLJTAMultipleDataSource jdsMgr = initJEPLJTAMultipleDataSource(boot,dsJTAFactory1,useFakeUserTxn);
        final JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(dsJTAFactory1.getDataSource());
        final JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(dsJTAFactory2.getDataSource());

        initData(jds1);

        final long waitMillisec = 5000; // 5 seconds is fine for concurrency

        final ContactDAO dao1 = new ContactDAO(jds1);
        Thread thread1 = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    long start = System.currentTimeMillis();
                    while(true)
                    {
                        JEPLTask<Contact> task = new JEPLTask<Contact>()
                        {
                            @Override
                            public Contact exec() throws Exception
                            {
                                TestContactDAOShared.testDAOSimpleTest(dao1);
    //System.out.println("thread1");
                                return null;
                            }
                        };
                        jdsMgr.exec(task,JEPLTransactionPropagation.REQUIRED);

                        long current = System.currentTimeMillis();
                        if ((current - start) > waitMillisec) break;
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    assertTrue(false);
                }
            }
        };

        final ContactDAO dao2 = new ContactDAO(jds2);
        Thread thread2 = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    long start = System.currentTimeMillis();
                    while(true)
                    {
                        JEPLTask<Contact> task = new JEPLTask<Contact>()
                        {
                            @Override
                            public Contact exec() throws Exception
                            {
                                TestContactDAOShared.testDAOSimpleTest(dao2);
    //System.out.println("thread2");
                                return null;
                            }
                        };
                        jdsMgr.exec(task,JEPLTransactionPropagation.REQUIRED);
                        long current = System.currentTimeMillis();
                        if ((current - start) > waitMillisec) break;
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    assertTrue(false);
                }
            }
        };

        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
    }


    public static JEPLJTAMultipleDataSource initJEPLJTAMultipleDataSource(JEPLBootJTA boot,DataSourceLoaderJTA dsJTAFactory1,boolean useFakeUserTxn)
    {
        if (!useFakeUserTxn)
        {
            boot.setUserTransaction(dsJTAFactory1.getUserTransaction());
            boot.setTransactionManager(dsJTAFactory1.getTransactionManager());
        }
        else
        {
            boot.setUserTransaction(boot.createJDBCUserTransaction());
            boot.setTransactionManager(null);
        }

        return boot.getJEPLJTAMultipleDataSource();
    }

    public static JEPLTask<Contact> createJEPLTask(final int num,final JEPLJTADataSource jds,final JEPLTransactionPropagation txnPropExpected)
    {
        return createJEPLTask(num,jds,txnPropExpected,null);
    }

    public static JEPLTask<Contact> createJEPLTask(final int num,final JEPLJTADataSource jds,final JEPLTransactionPropagation txnPropExpected,final Connection con)
    {
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                if (con != null)
                    assertTrue(con == jds.getCurrentJEPLConnection().getConnection());

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == txnPropExpected);
                if (num == 1)
                {
                    testDAO1(jds);
                    return null;
                }
                else
                {
                    return testDAO2(jds);
                }
            }
        };
        return task;
    }

    public static JEPLTask<Contact> createJEPLTaskDSMgr(final JEPLJTAMultipleDataSource jdsMgr,
            final JEPLJTADataSource jds1,final JEPLJTADataSource jds2,
            final JEPLTransactionPropagation txnPropExpected)
    {
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jdsMgr.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == txnPropExpected);

                testDAO(jds1,jds2);
                return null;
            }
        };
        return task;
    }

    public static void initData(final JEPLJTADataSource jds)
    {
        TestContactDAOShared.initDataSimpleTest(new ContactDAO(jds));
    }

    public static Contact testDAO(final JEPLJTADataSource jds1,final JEPLJTADataSource jds2)
    {
        ContactDAO dao1 = new ContactDAO(jds1);

        List<Contact> listContact = dao1.selectAll();
        assertTrue(!listContact.isEmpty());
        long id1 = listContact.get(0).getId();

        ContactDAO dao2 = new ContactDAO(jds2);

        listContact = dao2.selectAll();
        assertTrue(!listContact.isEmpty());
        long id2 = listContact.get(0).getId();

        assertTrue(id1 == id2);

        Contact cont = TestDAOShared.createContact();
        dao2.insert(cont);

        return cont;
    }

    public static Contact[] insertTwo(final JEPLJTADataSource jds1,final JEPLJTADataSource jds2)
    {
        Contact[] inserted = new Contact[2];
        ContactDAO dao1 = new ContactDAO(jds1);
        ContactDAO dao2 = new ContactDAO(jds2);

        Contact cont = TestDAOShared.createContact();
        dao1.insert(cont);
        inserted[0] = cont;

        cont = TestDAOShared.createContact();
        dao2.insert(cont);
        inserted[1] = cont;
        
        return inserted;
    }

    public static void testDAO1(final JEPLJTADataSource jds1)
    {
        TestContactDAOShared.testDAOSimpleTest(new ContactDAO(jds1));
    }

    public static Contact testDAO2(final JEPLJTADataSource jds2)
    {
        ContactDAO dao2 = new ContactDAO(jds2);

        List<Contact> listContact = dao2.selectAll();
        assertTrue(!listContact.isEmpty());

        Contact cont = TestDAOShared.createContact();
        dao2.insert(cont);

        return cont;
    }
}
