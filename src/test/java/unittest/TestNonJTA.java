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
import example.DataSourceFactoryOfLoaderJDBC;
import example.DataSourceLoader;
import example.dao.ContactDAO;
import example.model.Contact;
import java.sql.Connection;
import javax.sql.DataSource;
import jepl.*;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import unittest.shared.TestContactDAOShared;
import unittest.shared.TestContactDAOSharedNonJTA;


/**
 *
 * @author jmarranz
 */
public class TestNonJTA
{
    public TestNonJTA()
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
    public void someTest()
    {
        DataSourceLoader[] dsFactoryArr =
                DataSourceFactoryOfLoaderJDBC.getDataSourceFactoryOfLoaderJDBC().getDataSourceLoaderList();
        for(int i = 0; i < dsFactoryArr.length; i++)
        {
            DataSourceLoader dsFactory = dsFactoryArr[i];
            try
            {
                System.out.println("PROVIDER: " + dsFactory.getName());
                execTest(dsFactory.getDataSource());
            }
            finally
            {
                dsFactory.destroy();
            }
        }

    }


    public void execTest(DataSource ds)
    {
        CreateDBModel.createDB(ds);

        try
        {
            testGlobalJDBCAutoCommitDisabled(ds);

            testGlobalJDBCAutoCommitEnabled(ds);

            testGlobalJEPLConnectionListenerJDBCAutoCommitEnabled(ds);

            testJEPLTaskJDBCAutoCommitEnabledExplicit(ds);

            testJEPLTaskJDBCAutoCommitDisabledExplicit(ds);

            testJEPLTaskAnnotated(ds);

            testJEPLTaskWithJEPLConnectionListenerJDBCAutoCommitDisabled(ds);

            testJEPLTaskCallingGetCurrentJEPLConnection(ds);

            testJEPLTaskWithJEPLConnectionListenerJDBCAutoCommitDisabledExplicitRollback(ds);

            testJEPLTaskImplicitRollback(ds);

            testJEPLTaskNested(ds);

            testJEPLTaskUseGlobalDefaultTransactionMode(ds);

            testJEPLTaskWithJEPLConnectionListenerUsingJEPLTransaction(ds);

            testJEPLTaskManualTransactionIntoTask(ds);

            testListenersAsParams(ds);

            testStandAloneDAODAL(ds);

            testStandAloneDAODALInJEPLTask(ds);

            testMultithread(ds);
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


    public void testGlobalJDBCAutoCommitDisabled(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        jds.setDefaultAutoCommit(false);
        TestContactDAOShared.testDAO(new ContactDAO(jds));
    }

    public void testGlobalJDBCAutoCommitEnabled(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        jds.setDefaultAutoCommit(true);
        TestContactDAOShared.testDAO(new ContactDAO(jds));
    }

    public void testGlobalJEPLConnectionListenerJDBCAutoCommitEnabled(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        final boolean[] used = new boolean[]{ false };
        jds.addJEPLListener(new JEPLConnectionListener()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
                {
                    used[0] = true;
                    con.getConnection().setAutoCommit(true);
                }
            }
        );
        TestContactDAOShared.testDAO(new ContactDAO(jds));
        assertTrue(used[0]);
    }

    public void testJEPLTaskUseGlobalDefaultTransactionMode(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        assertTrue(jds.isDefaultAutoCommit());

        JEPLTask<Contact> task = createJEPLTask(jds,true);
        jds.exec(task);
    }

    public void testJEPLTaskJDBCAutoCommitEnabledExplicit(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        JEPLTask<Contact> task = createJEPLTask(jds,true);
        jds.exec(task,true);  // No transaction
    }

    public void testJEPLTaskJDBCAutoCommitDisabledExplicit(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        JEPLTask<Contact> task = createJEPLTask(jds,false);
        jds.exec(task,false); // Transaction
    }

    public void testJEPLTaskAnnotated(DataSource ds) throws Exception
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        jds.setDefaultAutoCommit(true); // Para asegurar que el valor por defecto no es transaccional y testear que en el primer test se lee el valor de la anotación

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalNonJTA // Por defecto es transaccional (false)
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(con.getAutoCommit() == false);
                return null;
            }
        };
        jds.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalNonJTA(autoCommit = false)
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(con.getAutoCommit() == false);
                return null;
            }
        };
        jds.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalNonJTA(autoCommit = true)
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(con.getAutoCommit() == false);
                return null;
            }
        };
        jds.exec(task,false);  // Ha de ganar a la anotación

    }


    public void testJEPLTaskWithJEPLConnectionListenerJDBCAutoCommitDisabled(DataSource ds)
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        jds.setDefaultAutoCommit(true); // Para intentar fastidiar nuestra configuración posterior con JEPLConnectionListener que tiene mayor prioridad

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalNonJTA(autoCommit = true)  // Para intentar fastidiar nuestra configuración posterior con JEPLConnectionListener que tiene mayor prioridad
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(con.getAutoCommit() == false);
                return null;
            }
        };

        final boolean[] used = new boolean[]{ false };
        jds.exec(task,new JEPLConnectionListener()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
                {
                    used[0] = true;
                    assertTrue(con.getConnection().getAutoCommit() == true);
                    con.getConnection().setAutoCommit(false);
                }
            }
        );
        assertTrue(used[0]);
    }

    public void testJEPLTaskCallingGetCurrentJEPLConnection(DataSource ds)
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                JEPLConnection jcon = jds.getCurrentJEPLConnection();
                jcon.getConnection().setAutoCommit(false); // transaction

                Contact contact = TestContactDAOShared.testDAO(new ContactDAO(jds));

                jcon.getConnection().commit();
                jcon.getConnection().setAutoCommit(true); // Para evitar el commit del framework

                return contact;
            }
        };
        jds.exec(task,true);
    }

    /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
     *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
     */
    public synchronized static void testJEPLTaskWithJEPLConnectionListenerJDBCAutoCommitDisabledExplicitRollback(DataSource ds)
    {
        final boolean[] used = new boolean[]{ false };

        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        JEPLTask<Contact> task = createJEPLTask(jds,false);

        // Test transaction manually setting autoCommit
        Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
                {
                    used[0] = true;
                    con.getConnection().setAutoCommit(false); // transaction

                    try
                    {
                        Contact contact = task.exec();
                        // Comprobamos que está en la base de datos antes de hacer rollback
                        TestContactDAOShared.testIsInDB(jds,contact);
                    }
                    finally
                    {
                        con.getConnection().rollback();
                    }
                }
            }
        );
        assertTrue(used[0]);
        TestContactDAOShared.testIsRollbacked(jds,contact);
    }

    /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
     *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
     */
    public synchronized static void testJEPLTaskImplicitRollback(DataSource ds)
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        final Contact[] contact = new Contact[1];
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                contact[0] = TestContactDAOShared.testDAO(new ContactDAO(jds));

                throw new Exception("SOME EXCEPTION");
            }
        };
        try
        {
            jds.exec(task,false);
            assertTrue(false); // No debe llegar aquí
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getCause().getMessage().equals("SOME EXCEPTION"));
            TestContactDAOShared.testIsRollbacked(jds,contact[0]);
        }
    }

    public static synchronized void testJEPLTaskWithJEPLConnectionListenerUsingJEPLTransaction(DataSource ds)
    {
        /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
         *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
         */

        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        final boolean[] used = new boolean[]{ false };

        JEPLTask<Contact> task = TestContactDAOShared.createJEPLTask(jds);

        // Test transaction using JEPLTransaction wrapper
        used[0] = false;
        Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
                {
                    used[0] = true;
                    con.getJEPLTransaction().begin();

                    try
                    {
                        Contact contact = task.exec();
                        // Comprobamos que está en la base de datos antes de hacer rollback
                        TestContactDAOShared.testIsInDB(jds,contact);
                    }
                    finally
                    {
                        con.getJEPLTransaction().rollback();
                    }
                }
            }
        );
        assertTrue(used[0]);
        TestContactDAOShared.testIsRollbacked(jds,contact);
    }


    public void testJEPLTaskManualTransactionIntoTask(DataSource ds)
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(!con.getAutoCommit());
                con.setAutoCommit(false); // transaction

                Contact contact = TestContactDAOShared.testDAO(new ContactDAO(jds));

                con.commit();

                //con.setDefaultAutoCommit(true);

                return contact;
            }
        };
        jds.exec(task,false);
    }

    public void testJEPLTaskNested(DataSource ds)
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        JEPLTask<Contact> task;

        // Test two nested tasks: outside non-txn, inside txn
        task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                final Connection con = jds.getCurrentJEPLConnection().getConnection();

                boolean autoCommit = con.getAutoCommit();
                assertTrue(autoCommit);
                JEPLTask<Contact> task = new JEPLTask<Contact>()
                {
                    @Override
                    public Contact exec() throws Exception
                    {
                        assertTrue(con == jds.getCurrentJEPLConnection().getConnection());

                        boolean autoCommit = jds.getCurrentJEPLConnection().getConnection().getAutoCommit();
                        assertTrue(!autoCommit);
                        return TestContactDAOShared.testDAO(new ContactDAO(jds));
                    }
                };
                Contact res = jds.exec(task,false); // transactional

                autoCommit = jds.getCurrentJEPLConnection().getConnection().getAutoCommit();
                assertTrue(autoCommit); // Se ha restaurado el valor

                return res;
            }
        };
        jds.exec(task,true); // not transactional

        // Test two nested tasks same txn
        task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                final Connection con = jds.getCurrentJEPLConnection().getConnection();

                boolean autoCommit = jds.getCurrentJEPLConnection().getConnection().getAutoCommit();
                assertTrue(!autoCommit);
                JEPLTask<Contact> task = new JEPLTask<Contact>()
                {
                    @Override
                    public Contact exec() throws Exception
                    {
                        assertTrue(con == jds.getCurrentJEPLConnection().getConnection());

                        boolean autoCommit = jds.getCurrentJEPLConnection().getConnection().getAutoCommit();
                        assertTrue(!autoCommit);
                        return TestContactDAOShared.testDAO(new ContactDAO(jds));
                    }
                };
                Contact res = jds.exec(task,false); // transactional

                autoCommit = jds.getCurrentJEPLConnection().getConnection().getAutoCommit();
                assertTrue(!autoCommit);

                return res;
            }
        };
        jds.exec(task,false); // transactional
    }

    public void testListenersAsParams(DataSource ds) //throws SystemException
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        new TestContactDAOSharedNonJTA().testListenersAsParams(jds);
    }

    public void testStandAloneDAODALInJEPLTask(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        TestContactDAOShared.testStandAloneDAODALInJEPLTask(jds);
    }

    public void testStandAloneDAODAL(DataSource ds)
    {
        JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);
        TestContactDAOShared.testStandAloneDAODAL(jds);
    }

    public void testMultithread(DataSource ds) throws Exception
    {
        final JEPLNonJTADataSource jds = createJEPLNonJTADataSource(ds);

        final ContactDAO dao = new ContactDAO(jds);
        TestContactDAOShared.initDataSimpleTest(dao);

        final long waitMillisec = 5000; // 5 seconds is fine for concurrency

        Runnable runnable = new Runnable()
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
                                TestContactDAOShared.testDAOSimpleTest(dao);
                                return null;
                            }
                        };
                        jds.exec(task);

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

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    public static JEPLNonJTADataSource createJEPLNonJTADataSource(DataSource ds)
    {
        JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
        return boot.createJEPLNonJTADataSource(ds);
    }

    public static JEPLTask<Contact> createJEPLTask(final JEPLNonJTADataSource jds,final boolean autoCommitExpected)
    {
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                assertTrue(con.getAutoCommit() == autoCommitExpected);

                return TestContactDAOShared.testDAO(new ContactDAO(jds));
            }
        };
        return task;
    }

}
