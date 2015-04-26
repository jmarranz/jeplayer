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

package manual;

import example.dao.ContactDAO;
import example.model.Contact;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import jepl.JEPLBootJTA;
import jepl.JEPLBootNonJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLCachedResultSet;
import jepl.JEPLConnection;
import jepl.JEPLConnectionListener;
import jepl.JEPLDAL;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLJTADataSource;
import jepl.JEPLJTAMultipleDataSource;
import jepl.JEPLNonJTADataSource;
import jepl.JEPLPreparedStatement;
import jepl.JEPLPreparedStatementListener;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDAO;
import jepl.JEPLTask;
import jepl.JEPLTransaction;
import jepl.JEPLTransactionPropagation;
import jepl.JEPLTransactionalJTA;
import jepl.JEPLTransactionalNonJTA;

/**
 *
 * @author jmarranz
 */
public class ReferenceManualCode
{
    public void insert_number()
    {
    JEPLDAO<Contact> dao = null;
    Contact contact = null;

    int key = dao.createJEPLDALQuery(
                "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?1, ?2, ?3)")
            .setParameter(1,contact.getEmail())
            .setParameter(2,contact.getName())
            .setParameter(3,contact.getPhone())
            .getGeneratedKey(int.class);
    }

    public void insert_name()
    {
    JEPLDAO<Contact> dao = null;
    Contact contact = null;

    int key = dao.createJEPLDALQuery(
                "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (:email, :name, :phone)")
            .setParameter("email",contact.getEmail())
            .setParameter("name",contact.getName())
            .setParameter("phone",contact.getPhone())
            .getGeneratedKey(int.class);
    }

    public void insert_mixed()
    {
    JEPLDAO<Contact> dao = null;
    Contact contact = null;

    int key = dao.createJEPLDALQuery(
                "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?, ?2, :phone)")
            .setParameter(1,contact.getEmail())
            .setParameter(2,contact.getName())
            .setParameter("phone",contact.getPhone())
            .getGeneratedKey(int.class);
    }

    public void Select_query_with_a_max_number_of_rows_using_a_listener_and_the_JEPLTask()
    {
    JEPLDataSource jds = null;
    jds.setPreparedStatementCached(false);
    // ...
    final int maxRows =   0;

    JEPLPreparedStatementListener<List<Contact>> listener =
            new JEPLPreparedStatementListener<List<Contact>>()
    {
        public void setupJEPLPreparedStatement(JEPLPreparedStatement jstmt,
                JEPLTask<List<Contact>> task) throws Exception
        {
            PreparedStatement stmt = jstmt.getPreparedStatement();
            stmt.setMaxRows(maxRows); // Now is not reused
        }
    };


    }

    public void free_hand_queries_1() throws SQLException
    {
    JEPLDataSource jds = null;
    JEPLDAL dal = jds.createJEPLDAL();        
        
    JEPLResultSet resSet = dal.createJEPLDALQuery(
            "SELECT COUNT(*) AS CO,AVG(ID) AS AV FROM CONTACT")
            .getJEPLResultSet();

    ResultSet rs = resSet.getResultSet();
    ResultSetMetaData metadata = rs.getMetaData();
    int ncols = metadata.getColumnCount();
    String[] colNames = new String[ncols];
    for(int i = 0; i < ncols; i++)
        colNames[i] = metadata.getColumnLabel(i + 1); // Empieza en 1        

    if (colNames.length != 2) throw new RuntimeException("UNEXPECTED");
    if (!colNames[0].equals("CO")) throw new RuntimeException("UNEXPECTED");
    if (!colNames[1].equals("AV")) throw new RuntimeException("UNEXPECTED");

    resSet.next();

    int count = rs.getInt(1);
    if (count != 2) throw new RuntimeException("UNEXPECTED");       
    count = rs.getInt("CO");
    if (count != 2) throw new RuntimeException("UNEXPECTED");

    float avg = rs.getFloat(1);
    if (avg <= 0) throw new RuntimeException("UNEXPECTED");       
    avg = rs.getFloat("AV");
    if (avg <= 0) throw new RuntimeException("UNEXPECTED");                      

    if (!resSet.isClosed()) throw new RuntimeException("UNEXPECTED");   
    }

    public void free_hand_queries_2()
    {
    JEPLDataSource jds = null;
    JEPLDAL dal = jds.createJEPLDAL();

    JEPLCachedResultSet resSet = dal.createJEPLDALQuery(
                "SELECT COUNT(*) AS CO,AVG(ID) AS AV FROM CONTACT")
                .getJEPLCachedResultSet();

    String[] colNames = resSet.getColumnLabels();
    if (colNames.length != 2) throw new RuntimeException("UNEXPECTED");
    if (!colNames[0].equals("CO")) throw new RuntimeException("UNEXPECTED");
    if (!colNames[1].equals("AV")) throw new RuntimeException("UNEXPECTED");
    if (resSet.size() != 1) throw new RuntimeException("UNEXPECTED");

    int count = resSet.getValue(1, 1, int.class); // Row 1, column 1
    if (count != 2) throw new RuntimeException("UNEXPECTED");
    count = resSet.getValue(1, "CO", int.class);
    if (count != 2) throw new RuntimeException("UNEXPECTED");

    float avg = resSet.getValue(1, 2, float.class); // Row 1, column 2
    if (avg <= 0) throw new RuntimeException("UNEXPECTED");
    avg = resSet.getValue(1, "AV", float.class);
    if (avg <= 0) throw new RuntimeException("UNEXPECTED");
    }

    public void task()
    {
    final JEPLDataSource jds = null;
    final ContactDAO dao = new ContactDAO(jds);
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            Contact contact = new Contact();
            contact.setName("A Contact object");
            contact.setPhone("9999999");
            contact.setEmail("contact@world.com");

            dao.insert(contact);

            Contact contact2 = dao.selectById(contact.getId());
            return contact2;
        }
    };
    Contact contact = jds.exec(task);
    //...
    }



    public void non_jta_transaction()
    {
    DataSource ds = null;
    JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
    JEPLNonJTADataSource jds = boot.createJEPLNonJTADataSource(ds);
    jds.setDefaultAutoCommit(false);
    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task);

    }

   public void non_jta_transaction2()
    {
    DataSource ds = null;
    JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
    JEPLNonJTADataSource jds = boot.createJEPLNonJTADataSource(ds);
    jds.setDefaultAutoCommit(true);
    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
       // @JEPLTransactionalNonJTA
        
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task);

    }

    @JEPLTransactionalNonJTA(autoCommit = false)
    public void non_jta_transaction2_2()
    {
    }

    @JEPLTransactionalNonJTA(autoCommit = true)
    public void non_jta_transaction2_3()
    {
    }

    public void non_jta_transaction3()
    {
    JEPLNonJTADataSource jds = null;
    jds.setDefaultAutoCommit(false);
    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task,true);
    }

    public void non_jta_transaction4()
    {
    JEPLDataSource jds = null;
    jds.addJEPLListener(new JEPLConnectionListener()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
            {
                 con.getConnection().setAutoCommit(true);
            }
        }
    );
    }

    public void non_jta_transaction5()
    {
    JEPLDataSource jds = null;
    JEPLTask<Contact> task = null;
    jds.exec(task,new JEPLConnectionListener()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
            {
                con.getConnection().setAutoCommit(false);
            }
        }
    );    
    }

    public void non_jta_transaction6()
    {
    JEPLDataSource jds = null;
    JEPLTask<Contact> task = null;
    jds.exec(task,new JEPLConnectionListener()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
            {
                con.getConnection().setAutoCommit(false); // transaction
                try
                {
                    task.exec();
                    con.getConnection().commit();
                }
                catch(Exception ex)
                {
                    con.getConnection().rollback();
                    throw ex;
                }
            }
        }
    );
    }

    public void non_jta_transaction7()
    {
    JEPLDataSource jds = null;
    JEPLTask<Contact> task = null;
    jds.exec(task,new JEPLConnectionListener()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
            {
                JEPLTransaction txn = con.getJEPLTransaction();
                txn.begin(); // Executes setDefaultAutoCommit(false);

                try
                {
                    task.exec();
                    txn.commit();
                }
                catch(Exception ex)
                {
                    txn.rollback();
                    throw ex;
                }
            }
        }
    );
    }

    public void non_jta_transaction8()
    {
    final JEPLNonJTADataSource jds = null;

    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            Connection con = jds.getCurrentJEPLConnection().getConnection();
            con.setAutoCommit(false); // transaction

            Contact contact = null; // Some persistent statements

            con.commit();

            con.setAutoCommit(true);

            return contact;
        }
    };
    jds.exec(task,false);
    }

    public void non_jta_nested()
    {
    final JEPLNonJTADataSource jds = null;
    JEPLTask<Contact> taskOutside = new JEPLTask<Contact>()
    {
        public Contact exec() throws Exception
        {
            JEPLTask<Contact> taskInside = new JEPLTask<Contact>()
            {
                public Contact exec() throws Exception
                {
                    return null; // Database operations
                }
            };
            return jds.exec(taskInside,false);
        }
    };
    jds.exec(taskOutside,false);
    }

    public void jta_transaction()
    {
    UserTransaction txn = null;
    TransactionManager txnMgr = null;
    DataSource ds = null;

    JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
    boot.setUserTransaction(txn);
    boot.setTransactionManager(txnMgr);

    JEPLJTADataSource jds = boot.createJEPLJTADataSource(ds);
    jds.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.REQUIRED);

    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task);
    }


    public void jta_transaction2_2()
    {
    JEPLJTADataSource jds = null;
    jds.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED);

    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        @JEPLTransactionalJTA
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task);
    }

    @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.REQUIRED)
    public void jta_transaction2_3()
    {
    }

    @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.NEVER)
    public void jta_transaction2_4()
    {
    }

    public void jta_transaction3()
    {
    JEPLJTADataSource jds = null;
    jds.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED);

    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        @JEPLTransactionalJTA(propagation = JEPLTransactionPropagation.NOT_SUPPORTED)
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    Contact contact = jds.exec(task,JEPLTransactionPropagation.REQUIRED);
    }

    public void jta_transaction4()
    {
    final JEPLJTADataSource jds = null;
    // ...
    JEPLTask<Contact> task = null;
    Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
            {
                UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();
                // txn.begin(); // transaction
                try
                {
                    Contact contact = task.exec();
                }
                finally
                {
                    txn.rollback();
                }
            }
        },JEPLTransactionPropagation.REQUIRED);
    }

    public void jta_transaction4_2()
    {
    JEPLJTADataSource jds = null;
    // ...
    JEPLTask<Contact> task = null;
    Contact contact = jds.exec(task,new JEPLConnectionListener<Contact>()
        {
            public void setupJEPLConnection(JEPLConnection con,JEPLTask<Contact> task) throws Exception
            {
                JEPLTransaction txn = con.getJEPLTransaction();
                if (!txn.isActive()) txn.begin(); 
                try
                {
                    Contact contact = task.exec();
                }
                finally
                {
                    txn.rollback();
                }
            }
        },JEPLTransactionPropagation.REQUIRED);
    }

    public void jta_transaction5() throws Exception
    {
    JEPLJTADataSource jds = null;
    UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();
    txn.begin();
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            Contact contact = null;
            return contact;
        }
    };
    jds.exec(task,JEPLTransactionPropagation.NEVER);
    txn.commit();
    }

    public void jta_transaction7() throws Exception
    {
    JEPLJTADataSource jds = null;
    UserTransaction txn = jds.getJEPLBootJTA().getUserTransaction();

    txn.begin();
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        public Contact exec() throws Exception
        {
            return null; // Database actions
        }
    };
    jds.exec(task,JEPLTransactionPropagation.REQUIRED);	// OR REQUIRED
    txn.commit();
    }

    public void jta_transaction8()
    {
    final JEPLJTADataSource jds = null;
    JEPLTask<Contact> taskOutside = new JEPLTask<Contact>()
    {
        public Contact exec() throws Exception
        {
            JEPLTask<Contact> taskInside = new JEPLTask<Contact>()
            {
                public Contact exec() throws Exception
                {
                    return null; // Database actions
                }
            };
            return jds.exec(taskInside,JEPLTransactionPropagation.SUPPORTS);
        }
    };
    jds.exec(taskOutside,JEPLTransactionPropagation.REQUIRED);
    }

    public void jta_fake()
    {
    JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
    boot.setUserTransaction(boot.createJDBCUserTransaction());
    }

    public void jta_multiple()
    {
    UserTransaction txn = null;
    TransactionManager txnMgr = null;
    DataSource ds1 = null;
    DataSource ds2 = null;

    JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
    boot.setUserTransaction(txn);
    boot.setTransactionManager(txnMgr);

    JEPLJTAMultipleDataSource jdsMgr = boot.getJEPLJTAMultipleDataSource();
    final JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(ds1);
    final JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(ds2);

    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        public Contact exec() throws Exception
        {
            // Persistent actions using jds1 and jds2
            return null;
        }
    };
    jdsMgr.exec(task);
    }

    public void jta_multiple_2()
    {
    UserTransaction txn = null;
    TransactionManager txnMgr = null;
    DataSource ds1 = null;
    DataSource ds2 = null;

    JEPLBootJTA boot = JEPLBootRoot.get().createJEPLBootJTA();
    boot.setUserTransaction(txn);
    boot.setTransactionManager(txnMgr);

    JEPLJTAMultipleDataSource jdsMgr = boot.getJEPLJTAMultipleDataSource();
    jdsMgr.setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.REQUIRED);
    
    final JEPLJTADataSource jds1 = boot.createJEPLJTADataSource(ds1);
    final JEPLJTADataSource jds2 = boot.createJEPLJTADataSource(ds2);

    }

    public void jta_multiple_3()
    {
    JEPLJTAMultipleDataSource jdsMgr = null;
    // ...
    JEPLTask<Contact> task = new JEPLTask<Contact>()
    {
        @Override
        @JEPLTransactionalJTA 
        public Contact exec() throws Exception
        {
            // Persistent actions using jds1 and jds2
            return null;
        }
    };
    jdsMgr.exec(task);
    }

    public void jta_multiple_4()
    {
    JEPLJTAMultipleDataSource jdsMgr = null;
    // ...
    JEPLTask<Contact> task = null;
    jdsMgr.exec(task,JEPLTransactionPropagation.MANDATORY);
    }

    public void uncached_resultset()
    {
    ContactDAO dao = null;
    JEPLResultSetDAO<Contact> resSetDAO = dao.selectAllResultSetDAO();
    if (resSetDAO.isClosed()) throw new RuntimeException("WRONG");
    while(resSetDAO.next())
    {
        Contact contact = resSetDAO.getObject();
        System.out.println("Contact: " + contact.getName());
    }
    // Now we know is closed
    if (!resSetDAO.isClosed()) throw new RuntimeException("WRONG");
    }

    public void uncached_resultset2()
    {
    ContactDAO dao = null;
    List<Contact> resSetDAO = dao.selectAllResultSetDAO();
    if (((JEPLResultSetDAO)resSetDAO).isClosed()) throw new RuntimeException("WRONG");
    for(Contact contact : resSetDAO) // Uses Iterator<Contact>
    {
        System.out.println("Contact: " + contact.getName());
    }
    // Now we know is closed
    if (!((JEPLResultSetDAO)resSetDAO).isClosed()) throw new RuntimeException("WRONG");
    }

    public void uncached_resultset3()
    {
    ContactDAO dao = null;
    List<Contact> resSetDAO = dao.selectAllResultSetDAO();
    Contact contact1 = resSetDAO.get(0); // Got from DB
    Contact contact2 = resSetDAO.get(0); // Got from internal list (same obj)
    if (contact1 != contact2) throw new RuntimeException("WRONG");
    }
}
