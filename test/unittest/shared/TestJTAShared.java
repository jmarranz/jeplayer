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
package unittest.shared;

import example.DataSourceLoaderJTA;
import example.dao.ContactDAO;
import example.model.Contact;
import java.sql.Connection;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLConnection;
import jepl.JEPLConnectionListener;
import jepl.JEPLException;
import jepl.JEPLJTADataSource;
import jepl.JEPLTask;
import jepl.JEPLTransactionPropagation;
import jepl.JEPLTransactionalJTA;
import static org.junit.Assert.*;

/**
 *
 * @author jmarranz
 */
public class TestJTAShared
{
    public TestJTAShared()
    {
    }


    public static void execTest(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn,String jtaProvider)
    {
        try
        {
            testJEPLTaskUseGlobalDefaultTransactionMode(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskGlobalSetDefaultJEPLTransactionPropagation(dsJTAFactory,useFakeUserTxn);
            
            testJEPLTaskTransactionModeExplicit(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskNestedAndTransactionModes(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskAnnotated(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskWithJEPLConnectionListenerTxnExplicitDemarcationExplicitRollback(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskImplicitRollback(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskWithJEPLConnectionListenerUsingJEPLTransaction(dsJTAFactory,useFakeUserTxn);

            testJEPLTaskManualTransactionIntoTask(dsJTAFactory,useFakeUserTxn);

            testListenersAsParams(dsJTAFactory,useFakeUserTxn);

            testStandAloneDAODAL(dsJTAFactory,useFakeUserTxn);

            testStandAloneDAODALInJEPLTask(dsJTAFactory,useFakeUserTxn);

            testMultithread(dsJTAFactory,useFakeUserTxn,jtaProvider);
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

    public static void testJEPLTaskUseGlobalDefaultTransactionMode(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        assertTrue(jds.getDefaultJEPLTransactionPropagation() == JEPLTransactionPropagation.REQUIRED);
        JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRED);
        jds.exec(task);
    }

    public static void testJEPLTaskGlobalSetDefaultJEPLTransactionPropagation(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        // El valor por defecto es JEPLTransactionPropagation.REQUIRED
        // que ya fue testeado por lo que cualquier otro valor es suficiente para testear
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        jds.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED);
        JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED);
        jds.exec(task);
    }

    public static void testJEPLTaskTransactionModeExplicit(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn) throws Exception
    {
        // Estudiamos si funcionan todos los modos
        
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        JEPLTask<Contact> task;

        UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction(); // Es porque puede ser null el parámetro txn
        TransactionManager txnMgr = jds.getJEPLBootJTA().getTransactionManager();

        // MANDATORY
        try
        {
            task = createJEPLTask(jds,JEPLTransactionPropagation.MANDATORY);
            jds.exec(task,JEPLTransactionPropagation.MANDATORY);
            assertTrue(false); // No debe llegar aqui
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getMessage().equals("Specified MANDATORY transaction propagation mode and there is no active transaction"));
        }

        txn.begin();
        task = createJEPLTask(jds,JEPLTransactionPropagation.MANDATORY);
        jds.exec(task,JEPLTransactionPropagation.MANDATORY);
        txn.commit();

        // REQUIRED
        task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRED);
        jds.exec(task,JEPLTransactionPropagation.REQUIRED);

        // Idem pero con un txn ya arrancada
        txn.begin();
        task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRED);
        jds.exec(task,JEPLTransactionPropagation.REQUIRED);
        txn.commit();

        // REQUIRES_NEW
        task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW);
        jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);

        // Probamos REQUIRES_NEW pero con una transacción activa, el comportamiento
        // es el de suspender la transacción actual con el TransactionManager y abrir otra

        if (txnMgr != null)
        {
            txn.begin();
            task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW);
            jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);
            txn.commit();
        }
        else
        {
            try
            {
                txn.begin();
                task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW);
                jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);
                assertTrue(false); // No debe llegar aqui
                //txn.commit();
            }
            catch(JEPLException ex)
            {
                assertTrue(ex.getMessage().equals("Specified REQUIRES_NEW transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)"));
                txn.rollback();
            }
        }

        // SUPPORTS
        task = createJEPLTask(jds,JEPLTransactionPropagation.SUPPORTS);
        jds.exec(task,JEPLTransactionPropagation.SUPPORTS);

        txn.begin();
        task = createJEPLTask(jds,JEPLTransactionPropagation.SUPPORTS);
        jds.exec(task,JEPLTransactionPropagation.SUPPORTS);
        txn.commit();

        // NOT_SUPPORTED
        task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED);
        jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);

        // Probamos NOT_SUPPORTED pero con una transacción activa, el comportamiento
        // es el de suspender la transacción actual con el TransactionManager y abrir otra
        txnMgr = jds.getJEPLBootJTA().getTransactionManager();
        if (txnMgr != null)
        {
            txn.begin();
            task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED);
            jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);
            txn.commit();
        }
        else
        {
            try
            {
                txn.begin();
                task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED);
                jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);
                assertTrue(false); // No debe llegar aqui
                //txn.commit();
            }
            catch(JEPLException ex)
            {
                assertTrue(ex.getMessage().equals("Specified NOT_SUPPORTED transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)"));
                txn.rollback();
            }
        }

        // NEVER
        task = createJEPLTask(jds,JEPLTransactionPropagation.NEVER);
        jds.exec(task,JEPLTransactionPropagation.NEVER);

        try
        {
            txn.begin();
            task = createJEPLTask(jds,JEPLTransactionPropagation.NEVER);
            jds.exec(task,JEPLTransactionPropagation.NEVER);
            assertTrue(false); // No debe llegar aqui
            //txn.commit();
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getMessage().equals("Specified NEVER transaction propagation mode and there is an active transaction"));
            txn.rollback();
        }   
    }

    public static void testJEPLTaskNestedAndTransactionModes(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn) throws Exception
    {
        // Estudiamos si funcionan todos los modos en tasks anidados

        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        JEPLTask<Contact> taskOutside;

        UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();

        // MANDATORY
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                try
                {
                    JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.MANDATORY,con);
                    jds.exec(task,JEPLTransactionPropagation.MANDATORY);
                    assertTrue(false); // No debe llegar aqui
                }
                catch(JEPLException ex)
                {
                    assertTrue(ex.getMessage().equals("Specified MANDATORY transaction propagation mode and there is no active transaction"));
                }

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
            
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.MANDATORY,con);
                jds.exec(task,JEPLTransactionPropagation.MANDATORY);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);

                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);

        // REQUIRED
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRED,con);
                jds.exec(task,JEPLTransactionPropagation.REQUIRED);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        // Idem pero con un txn ya arrancada
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRED,con);
                jds.exec(task,JEPLTransactionPropagation.REQUIRED);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);

        // REQUIRES_NEW
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW,con);
                jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        // Probamos REQUIRES_NEW pero con una transacción activa, el comportamiento
        // es el de suspender la transacción actual con el TransactionManager y abrir otra
        TransactionManager txnMgr = jds.getJEPLBootJTA().getTransactionManager();
        if (txnMgr != null)
        {
            taskOutside = new JEPLTask<Contact>()
            {
                public Contact exec() throws Exception
                {
                    Connection con = jds.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW,con);
                    jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);

                    JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                    assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                    return null;
                }
            };
            jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);                
        }
        else
        {
            try
            {
                taskOutside = new JEPLTask<Contact>()
                {
                    public Contact exec() throws Exception
                    {
                        Connection con = jds.getCurrentJEPLConnection().getConnection();
                        JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.REQUIRES_NEW,con);
                        jds.exec(task,JEPLTransactionPropagation.REQUIRES_NEW);

                        JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                        assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                        return null;
                    }
                };
                jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);
                assertTrue(false); // No debe llegar aqui
            }
            catch(JEPLException ex)
            {
                assertTrue(ex.getMessage().equals("Specified REQUIRES_NEW transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)"));
                // Automáticamente se ha hecho un rollback
                assertTrue(txn.getStatus() == Status.STATUS_NO_TRANSACTION);
            }
        }


        // SUPPORTS
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.SUPPORTS,con);
                jds.exec(task,JEPLTransactionPropagation.SUPPORTS);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.SUPPORTS,con);
                jds.exec(task,JEPLTransactionPropagation.SUPPORTS);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);

        // NOT_SUPPORTED
        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED,con);
                jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        // Probamos NOT_SUPPORTED pero con una transacción activa, el comportamiento
        // es el de suspender la transacción actual con el TransactionManager y abrir otra
        txnMgr = jds.getJEPLBootJTA().getTransactionManager();
        if (txnMgr != null)
        {
            taskOutside = new JEPLTask<Contact>()
            {
                public Contact exec() throws Exception
                {
                    Connection con = jds.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED,con);
                    jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);

                    JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                    assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                    return null;
                }
            };
            jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);
        }
        else
        {
            try
            {
                taskOutside = new JEPLTask<Contact>()
                {
                    public Contact exec() throws Exception
                    {
                        Connection con = jds.getCurrentJEPLConnection().getConnection();
                        JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NOT_SUPPORTED,con);
                        jds.exec(task,JEPLTransactionPropagation.NOT_SUPPORTED);

                        JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                        assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                        return null;
                    }
                };
                jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);
                assertTrue(false); // No debe llegar aqui
            }
            catch(JEPLException ex)
            {
                assertTrue(ex.getMessage().equals("Specified NOT_SUPPORTED transaction propagation mode, there is an active transaction and this UserTransaction cannot be suspended (is not a TransactionManager)"));
                // Automáticamente se ha hecho un rollback
                assertTrue(txn.getStatus() == Status.STATUS_NO_TRANSACTION);
            }
        }

        // NEVER

        taskOutside = new JEPLTask<Contact>()
        {
            public Contact exec() throws Exception
            {
                Connection con = jds.getCurrentJEPLConnection().getConnection();
                JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NEVER,con);
                jds.exec(task,JEPLTransactionPropagation.NEVER);

                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);
                return null;
            }
        };
        jds.exec(taskOutside,JEPLTransactionPropagation.NEVER);

        try
        {
            taskOutside = new JEPLTask<Contact>()
            {
                public Contact exec() throws Exception
                {
                    Connection con = jds.getCurrentJEPLConnection().getConnection();
                    JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.NEVER,con);
                    jds.exec(task,JEPLTransactionPropagation.NEVER);

                    JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                    assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                    return null;
                }
            };
            jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);
            assertTrue(false); // No debe llegar aqui
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getMessage().equals("Specified NEVER transaction propagation mode and there is an active transaction"));
            // Automáticamente se ha hecho un rollback
            assertTrue(txn.getStatus() == Status.STATUS_NO_TRANSACTION);
        } 
    }

    public static void testJEPLTaskAnnotated(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn) throws Exception
    {
        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        jds.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED); // Para asegurar que el valor por defecto no es REQUIRED y testear que en el primer test se lee el valor de la anotación

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA // Por defecto es REQUIRED
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jds.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.REQUIRES_NEW)
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRES_NEW);
                return null;
            }
        };
        jds.exec(task);

        task = new JEPLTask<Contact>()
        {
            @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.NEVER)
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.REQUIRED);
                return null;
            }
        };
        jds.exec(task,JEPLTransactionPropagation.REQUIRED);  // Ha de ganar a la anotación

    }

    /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
     *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
     */
    public synchronized static void testJEPLTaskWithJEPLConnectionListenerTxnExplicitDemarcationExplicitRollback(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        final boolean[] used = new boolean[]{ false };

        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        JEPLTask<Contact> task = createJEPLTask(jds,JEPLTransactionPropagation.SUPPORTS);

        // Test transaction manually
        Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
                {
                    used[0] = true;

                    // El método getCurrentJEPLTransactionPropagation() actúa como "hint"
                    // de la propagación que se aplicaría si no lo hiciéramos manualmente aquí
                    JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                    assertTrue(txnProp == JEPLTransactionPropagation.SUPPORTS);

                    UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();
                    txn.begin(); // transaction

                    try
                    {
                        Contact contact = task.exec();
                        // Comprobamos que está en la base de datos antes de hacer rollback
                        TestContactDAOShared.testIsInDB(jds,contact);
                    }
                    finally
                    {
                        txn.rollback();
                    }
                }
            },JEPLTransactionPropagation.SUPPORTS
        );
        assertTrue(used[0]);
        TestContactDAOShared.testIsRollbacked(jds,contact);
    }

    /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
     *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
     */
    public synchronized static void testJEPLTaskImplicitRollback(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);

        final Contact[] contact = new Contact[1];
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                contact[0] = TestContactDAOShared.testDAO(new ContactDAO(jds));

                if (true) throw new Exception("SOME EXCEPTION");

                return null;
            }
        };
        try
        {
            jds.exec(task,JEPLTransactionPropagation.REQUIRED);
            assertTrue(false); // No debe llegar aquí
        }
        catch(JEPLException ex)
        {
            assertTrue(ex.getCause().getMessage().equals("SOME EXCEPTION"));
            TestContactDAOShared.testIsRollbacked(jds,contact[0]);
        }
    }

    public static void testJEPLTaskWithJEPLConnectionListenerUsingJEPLTransaction(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        /** Este test debe ser monohilo porque se testea el rollback y hay que evitar que otro
         *  hilo inserte el elemento (mismo id) que estamos comprobando que no se ha insertado.
         */

        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);

        final boolean[] used = new boolean[]{ false };

        JEPLTask<Contact> task = TestContactDAOShared.createJEPLTask(jds);

        // Test transaction using JEPLTransaction wrapper
        used[0] = false;
        Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
                {
                    used[0] = true;
                    assertFalse(con.getJEPLTransaction().isActive());
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
            },JEPLTransactionPropagation.SUPPORTS
        );
        assertTrue(used[0]);
        TestContactDAOShared.testIsRollbacked(jds,contact);
    }

    public static void testJEPLTaskManualTransactionIntoTask(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);

        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                JEPLTransactionPropagation txnProp = jds.getCurrentJEPLTransactionPropagation();
                assertTrue(txnProp == JEPLTransactionPropagation.NEVER);

                UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();
                txn.begin();

                Contact contact = TestContactDAOShared.testDAO(new ContactDAO(jds));

                txn.commit();

                return contact;
            }
        };
        jds.exec(task,JEPLTransactionPropagation.NEVER);
    }

    public static void testListenersAsParams(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn) throws SystemException
    {
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);

        // No debe haber una transacción abierta previamente en este test
        UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();
        if (txn != null && txn.getStatus() != Status.STATUS_NO_TRANSACTION)
            throw new RuntimeException("Unexpected test parameters");

        TestContactDAOSharedJTA testContact = new TestContactDAOSharedJTA();
        testContact.testListenersAsParams(jds);
    }
 
    public static void testStandAloneDAODALInJEPLTask(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        TestContactDAOShared.testStandAloneDAODALInJEPLTask(jds);
    }

    public static void testStandAloneDAODAL(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        TestContactDAOShared.testStandAloneDAODAL(jds);
    }

    public static void testMultithread(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn,String jtaProvider) throws Exception
    {
        final JEPLJTADataSource jds = createJEPLJTADataSource(dsJTAFactory,useFakeUserTxn);
        
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
                        jds.exec(task,JEPLTransactionPropagation.REQUIRED);

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

    public static JEPLJTADataSource createJEPLJTADataSource(DataSourceLoaderJTA dsJTAFactory,boolean useFakeUserTxn)
    {
        JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
        if (!useFakeUserTxn)
        {
            boot.setUserTransaction(dsJTAFactory.getUserTransaction());
            boot.setTransactionManager(dsJTAFactory.getTransactionManager());
        }
        else
        {
            boot.setUserTransaction(boot.createJDBCUserTransaction());
            boot.setTransactionManager(null);
        }
        return boot.createJEPLJTADataSource(dsJTAFactory.getDataSource());
    }

    public static JEPLTask<Contact> createJEPLTask(final JEPLJTADataSource jds,final JEPLTransactionPropagation txnPropExpected)
    {
        return createJEPLTask(jds,txnPropExpected,null);
    }

    public static JEPLTask<Contact> createJEPLTask(final JEPLJTADataSource jds,final JEPLTransactionPropagation txnPropExpected,final Connection con)
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

                return TestContactDAOShared.testDAO(new ContactDAO(jds));
            }
        };
        return task;
    }
}
