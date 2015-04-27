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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jepl.JEPLCachedResultSet;
import jepl.JEPLConnection;
import jepl.JEPLConnectionListener;
import jepl.JEPLDAL;
import jepl.JEPLDAO;
import jepl.JEPLDAOQuery;
import jepl.JEPLDataSource;
import jepl.JEPLException;
import jepl.JEPLNonJTADataSource;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDAO;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLTask;
import example.dao.ContactDAO;
import example.loadmanually.DataSourceLoaderManualLoad;
import example.model.Contact;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import jepl.JEPLTransactionPropagation;

/**
 *
 * @author jmarranz
 */
public abstract class TestContactDAOShared
{
    public final static boolean TEST_PERFORMANCE = false;

    public static JEPLTask<Contact> createJEPLTask(final JEPLDataSource jds)
    {
        JEPLTask<Contact> task = new JEPLTask<Contact>()
        {
            @Override
            public Contact exec() throws Exception
            {
                return testDAO(new ContactDAO(jds));
            }
        };
        return task;
    }

    public static void testStandAloneDAODAL(JEPLDataSource jds)
    {
        JEPLDAL dal = jds.createJEPLDAL();
        JEPLDAO<Contact> dao = jds.createJEPLDAO(Contact.class);
        testStandAloneDAODAL(dal,dao);
    }    

    public static void testStandAloneDAODALInJEPLTask(final JEPLDataSource jds)
    {
        JEPLTask<Object> task = new JEPLTask<Object>()
        {
            @Override
            public Object exec() throws Exception
            {
                testStandAloneDAODAL(jds.createJEPLDAL(),jds.createJEPLDAO(Contact.class));
                return null;
            }
        };
        jds.exec(task);
    }

    public void testListenersAsParams(final JEPLDataSource jds) 
    {
        // NO debe haber una transacción abierta previamente en este test, ni dentro de un task
        assertTrue(jds.getCurrentJEPLConnection() == null);
        
        // Tables empty initialization
        // because delete actions are tricky, doing manually (testing delete later)

        ContactDAO dao = new ContactDAO(jds);
        clearTables(dao);

        testJEPLConnectionListenerAsParameter(dao);

        testImplicitUpdateDAOListener(dao);
        
        testJDBCResultSetDALListenerAsParameter(dao);

        testJDBCResultSetDAOListenerAsParameter(dao);

        testPreparedStatementListenerAsParameter(dao);
    }

    public static void testPreparedStatementListenerAsParameter(ContactDAO dao)
    {
        // Test PreparedStatementListener as parameter
        List<Contact> list = dao.selectAll();
        assertTrue(list.size() > 1);
        if (!DataSourceLoaderManualLoad.android) // PreparedStatement.setMaxRows is not implemented in SQLDroid
        {        
	        list = dao.selectAllStatementListenerMaxRows( 1 );
	        assertTrue(list.size() == 1);
	        list = dao.selectAllStatementListenerMaxRows( 1 ); // Repeating to ensure caching the statement
	        assertTrue(list.size() == 1);
        }
        list = dao.selectAll(); // Same query as before, statement was cached and reused
        assertTrue(list.size() > 1); // Check whether maxRows filter was removed
    }

    public static void testJDBCResultSetDAOListenerAsParameter(ContactDAO dao)
    {
        // Test JEPLResultSetDAOListener as parameter
        dao.deleteAll();
        Contact cont = TestDAOShared.createContact();
        dao.insert(cont);
        Contact cont2 = TestDAOShared.createContact();
        dao.insert(cont2);
        int count = dao.selectAll().size();
        assertTrue(count == 2);

        List<Contact> list;

        list = dao.selectAllExplicitResultSetListener();
        assertTrue(list.size() == 2);

        list = dao.selectAllExplicitResultSetDAOListenerBean();
        assertTrue(list.size() == 2);
        TestDAOShared.checkContact(list.get(0));
        TestDAOShared.checkContact(list.get(1));

        list = dao.selectAllExplicitResultSetDAOListenerBeanWithMapper();
        assertTrue(list.size() == 2);
        TestDAOShared.checkContact(list.get(0));
        TestDAOShared.checkContact(list.get(1));

        // Test JEPLResultSetDAOListener as parameter, query a range
        for(int i = 0; i < 10; i++)
        {
            cont = TestDAOShared.createContact();
            dao.insert(cont);
        }
        if (!DataSourceLoaderManualLoad.android) // ResultSet.absolute() is not implemented in SQLDroid
        {        
	        list = dao.selectAllExplicitResultSetListenerRange(2,4);
	        assertTrue(list.size() == 2);
	        list = dao.selectAllExplicitResultSetListenerRange2(2,4);
	        assertTrue(list.size() == 2);
        }
        
        // Test JEPLDAOQuery.setFirstResult/setMaxResults
        //if (!DataSourceLoaderManualLoad.androidSQLDroid) // ResultSet.absolute() is not implemented in SQLDroid
        {        
	        list = dao.selectJEPLDAOQueryRange(2,4);
	        assertTrue(list.size() == 2);
	        list = dao.selectJEPLDAOQueryRange(1,3);
	        assertTrue(list.size() == 2);
	        List<Contact> list2 = dao.selectAll();
	        assertTrue(list.get(0).getId() == list2.get(0).getId());
	        assertTrue(list.get(1).getId() == list2.get(1).getId());
        }
    }

    public static void testImplicitUpdateDAOListener(ContactDAO dao)
    {
        // Test JDBCResultSetDALListener as parameter
        Contact cont = TestDAOShared.createContact();
        dao.insertImplicitUpdateDAOListener(cont);
        dao.delete(cont);
    }        
    
    public static void testJDBCResultSetDALListenerAsParameter(ContactDAO dao)
    {
        // Test JDBCResultSetDALListener as parameter
        Contact cont = TestDAOShared.createContact();
        dao.insertExplicitResultSetListener(cont);
        dao.delete(cont);
    }
    
    public void testJEPLConnectionListenerAsParameter(ContactDAO dao)
    {
        JEPLDAL dal = dao.getJEPLDAO();
        final JEPLDataSource jds = dal.getJEPLDataSource();
        
        final boolean[] used = new boolean[1];
        
        // Test JEPLConnectionListener as parameter autoCommit = true (only non-JTA)
        used[0] = false;
        dal.createJEPLDALQuery("DELETE FROM CONTACT")
            .addJEPLListener(new JEPLConnectionListener()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask task) throws Exception
                {
                    if (jds instanceof JEPLNonJTADataSource) // Case Non-JTA
                    {
                        con.getConnection().setAutoCommit(true);
                    }
                    else
                    {
                        // JTA: nothing interesting in this test
                    }

                    used[0] = true;
                }
            }).executeUpdate();

        assertTrue(used[0]);

        // Test JEPLConnectionListener as parameter controlling fully the lifecycle of transaction
        Contact cont = TestDAOShared.createContact();
        dao.insert(cont);
        assertTrue(cont.getId() != 0);
        used[0] = false;
        int res = dal.createJEPLDALQuery("DELETE FROM CONTACT")
            .addJEPLListener(new JEPLConnectionListener<Integer>()
            {
                public void setupJEPLConnection(JEPLConnection con,JEPLTask<Integer> task) throws Exception
                {
                	testSetupJEPLConnectionCommit(con,task,used);
                }
            }).executeUpdate();

        assertTrue(used[0]);
        if (!DataSourceLoaderManualLoad.android)
        	assertTrue(res > 0);
        int count = dao.selectCount();
        assertTrue(count == 0);

        // Test JEPLConnectionListener as parameter controlling fully the lifecycle,
        // forcing a rollback
        cont = TestDAOShared.createContact();
        dao.insert(cont);
        assertTrue(cont.getId() != 0);
        used[0] = false;
        try
        {
            dal.createJEPLDALQuery("DELETE FROM CONTACT")
                .addJEPLListener(new JEPLConnectionListener<Integer>()
                {
                    public void setupJEPLConnection(JEPLConnection con,JEPLTask<Integer> task) throws Exception
                    {
                    	testSetupJEPLConnectionForcedRollback(con,task,used);
                    }
                }).executeUpdate();
        }
        catch(JEPLException ex)
        {
            assertTrue(used[0]);
        }
        count = dao.selectCount();
        assertTrue(count > 0);
    }

    protected abstract void testSetupJEPLConnectionCommit(JEPLConnection con,JEPLTask<Integer> task,boolean[] used) throws Exception;
    protected abstract void testSetupJEPLConnectionForcedRollback(JEPLConnection con,JEPLTask<Integer> task,boolean[] used) throws Exception;
    
    
    public static void testIsRollbacked(JEPLDataSource jds,Contact contact)
    {
        ContactDAO dao = new ContactDAO(jds);
        Contact contact2 = dao.selectById(contact.getId());
        assertTrue(contact2 == null);
    }

    public static void testIsInDB(JEPLDataSource jds,Contact contact)
    {
        ContactDAO dao = new ContactDAO(jds);
        Contact contact2 = dao.selectById(contact.getId());
        assertTrue(contact2 != null);
    }

    public static void clearTables(ContactDAO dao)
    {
        // Tables empty initialization
        // because delete actions are tricky we're doing manually (testing delete in another place)
        JEPLDAL dal = dao.getJEPLDAO();
        dal.createJEPLDALQuery("DELETE FROM PERSON").executeUpdate();
        dal.createJEPLDALQuery("DELETE FROM COMPANY").executeUpdate();
        dal.createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();
        
        List<Contact> listContact = dao.selectAll();
        assertTrue(listContact.isEmpty());
    }

    public static void initDataSimpleTest(ContactDAO dao)
    {
        clearTables(dao);

        Contact cont = TestDAOShared.createContact();
        dao.insert(cont);
        assertTrue(cont.getId() != 0);
    }

    public static void testDAOSimpleTest(ContactDAO dao)
    {
        List<Contact> listContact = dao.selectAll();
        assertTrue(!listContact.isEmpty());
    }

    public static Contact testDAO(ContactDAO dao)
    {
        clearTables(dao);

        JEPLDAL dal = dao.getJEPLDAO();
        
        Contact cont = testGetGeneratedKeyAndGetSingleResult(dao);

        Contact cont2 = testGetResultList(cont,dao);

        testGetJEPLResultSet(dal);
        
        testGetJEPLResultSetDAO(dao);
        
        testGetJEPLCachedResultSet(dal);

        testGetOneRowFromSingleField(dao);

        testExecuteUpdate(cont,cont2,dao);

        // To check rollback
        cont = TestDAOShared.createContact();
        dao.insert(cont);
        cont2 = dao.selectById(cont.getId());
        assertTrue(cont2 != null);
        assertTrue(cont.getId() == cont2.getId());
        return cont;
    }

    public static void testExecuteUpdate(Contact cont,Contact cont2,ContactDAO dao)
    {
        // Test executeUpdate (update)
        cont.setName("A Contact object CHANGED");
        dao.update(cont);
        cont2 = dao.selectById(cont.getId());
        assertTrue(cont2.getName().equals("A Contact object CHANGED"));

        // Test executeUpdate (delete)
        cont = dao.selectById(cont.getId());
        assertTrue(cont != null);        
        boolean res = dao.delete(cont);
        if (!DataSourceLoaderManualLoad.android)        
        	assertTrue(res);
        cont = dao.selectById(cont.getId());
        assertTrue(cont == null);

        // Test executeUpdate (deleteAll, remains one)
        List<Contact> list = dao.selectAll();
        assertFalse(list.isEmpty());
        dao.deleteAll();
        list = dao.selectAll();
        assertTrue(list.isEmpty());
    }

    public static void testGetOneRowFromSingleField(ContactDAO dao)
    {
        // Test getOneRowFromSingleField
        int count = dao.selectCount();
        assertTrue(count == 2);
    }

    public static void testGetJEPLResultSet(final JEPLDAL dal)
    {          
        // Test getJEPLResultSet()
        // Se necesita una conexión abierta (un task) para que funcione el JEPLResultSet        
        if (dal.getJEPLDataSource().getCurrentJEPLConnection() != null)
        {                
            try
            {
                // Test getJEPLResultSet
                JEPLResultSet resSet = dal.createJEPLDALQuery(
                        "SELECT COUNT(*) AS CO,AVG(ID) AS AV FROM CONTACT")
                        .getJEPLResultSet();

                assertFalse(resSet.isClosed());                
                
                ResultSet rs = resSet.getResultSet();
                ResultSetMetaData metadata = rs.getMetaData();
                int ncols = metadata.getColumnCount();
                String[] colNames = new String[ncols];
                for(int i = 0; i < ncols; i++)
                    colNames[i] = metadata.getColumnLabel(i + 1); // Empieza en 1                     
                
                assertTrue(colNames.length == 2);
                assertTrue(colNames[0].equals("CO"));
                assertTrue(colNames[1].equals("AV"));

                assertTrue(rs.getRow() == 0);                 
                
                assertFalse(resSet.isClosed());

                resSet.next();

                assertTrue(rs.getRow() == 1);

                int count = rs.getInt(1);
                assertTrue(count == 2);       
                count = rs.getInt("CO");
                assertTrue(count == 2);

                float avg = rs.getFloat(1);
                assertTrue(avg > 0);        
                avg = rs.getFloat("AV");
                assertTrue(avg > 0);                       
                    
            
                assertFalse(resSet.next());                
                assertTrue(resSet.isClosed());                
         
                assertTrue(resSet.count() == 1);                
            }
            catch(SQLException ex)
            {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            
        }

    }    
    
    public static void testGetJEPLCachedResultSet(JEPLDAL dal)
    {
        // Test getJEPLCachedResultSet
        JEPLCachedResultSet resSet = dal.createJEPLDALQuery(
                "SELECT COUNT(*) AS CO,AVG(ID) AS AV FROM CONTACT")
                .getJEPLCachedResultSet();
        String[] colNames = resSet.getColumnLabels();
        assertTrue(colNames.length == 2);
        assertTrue(colNames[0].equals("CO"));
        assertTrue(colNames[1].equals("AV"));
        assertTrue(resSet.size() == 1);

        int count = resSet.getValue(1, 1, int.class); // Row 1, column 1
        assertTrue(count == 2);
        count = resSet.getValue(1, "CO", int.class);
        assertTrue(count == 2);

        float avg = resSet.getValue(1, 2, float.class); // Row 1, column 2
        assertTrue(avg > 0);
        avg = resSet.getValue(1, "AV", float.class);
        assertTrue(avg > 0);
    }

    public static void testGetJEPLResultSetDAO(ContactDAO dao)
    {
        // Test getJEPLResultSetDAO()
        // Se necesita una conexión abierta (un task) para que funcione el JEPLResultSetDAO
        JEPLDAL dal = dao.getJEPLDAO();
        if (dal.getJEPLDataSource().getCurrentJEPLConnection() != null)
        {
            List<Contact> list = new LinkedList<Contact>();
            JEPLResultSetDAO<Contact> resSetDAO = dao.selectAllResultSetDAO();
            assertFalse(resSetDAO.isClosed());
            while(resSetDAO.next())
            {
                Contact contact = resSetDAO.getObject();
                list.add(contact);
            }
            assertTrue(((JEPLResultSetDAO<Contact>)resSetDAO).isClosed());
            assertTrue(list.size() == 2);
            assertFalse(((List<Contact>)resSetDAO).isEmpty());

            list.clear();
            List<Contact> resSetDAOList = dao.selectAllResultSetDAO();
            for(Contact contact : resSetDAOList)
            {
                list.add(contact);
            }
            assertTrue(((JEPLResultSetDAO<Contact>)resSetDAOList).isClosed());
            assertTrue(list.size() == 2);

            list.clear();
            resSetDAOList = dao.selectAllResultSetDAO();
            // Expected two results
            Contact contact1,contact2;
            contact1 = resSetDAOList.get(0); // Gets from DB
            assertTrue(contact1 != null);
            contact2 = resSetDAOList.get(0); // Gets from internal list (same obj)
            assertTrue(contact1 == contact2);
            contact1 = resSetDAOList.get(1);
            assertFalse(contact1.getId() == contact2.getId());
            ((JEPLResultSetDAO<Contact>)resSetDAOList).close();
            assertTrue(((JEPLResultSetDAO<Contact>)resSetDAOList).isClosed());
            assertTrue(resSetDAOList.size() == 2);


            list.clear();
            resSetDAOList = dao.selectAllResultSetDAO();
            for(ListIterator<Contact> it = resSetDAOList.listIterator( 1 ); it.hasNext(); )
            {
                Contact cont = it.next();
                list.add(cont);
            }
            assertTrue(((JEPLResultSetDAO<Contact>)resSetDAOList).isClosed());
            assertTrue(list.size() == 1);

            resSetDAOList = dao.selectAllResultSetDAO();
            int size;
            try
            {
                size = resSetDAOList.size();
                assertTrue(size == 2);
            }
            catch(JEPLException ex)
            {
                // This exception is expected when running NetBeans debugger and ResultSet
                // not closed (size() is disabled in this context)
                assertTrue(!((JEPLResultSetDAO<Contact>)resSetDAOList).isClosed());
                boolean match = ex.getMessage().startsWith("size() method cannot be called in debug mode and ResultSet not closed");
                assertTrue(match);
            }

            assertTrue(((JEPLResultSetDAO<Contact>)resSetDAOList).isClosed());
            assertTrue(resSetDAOList.size() == 2);
        }
    }

    public static Contact testGetResultList(Contact cont,ContactDAO dao)
    {
        // Test getResultList
        Contact cont2 = new Contact();
        cont2.setName("Another Contact object");
        cont2.setPhone("2222222");
        cont2.setEmail("bye@world.com");
        dao.insert(cont2);
        List<Contact> list = dao.selectAll();
        assertTrue(list.size() == 2);
        while(!list.isEmpty())
        {
            int id = list.get(0).getId();
            assertTrue(cont.getId() == id || cont2.getId() == id);
            list.remove(0);
        }

        if (TEST_PERFORMANCE) // Simple test for performance
        {
            long start = System.currentTimeMillis();
            for(int i = 0; i < 50000; i++)
                list = dao.selectAll();
            long end = System.currentTimeMillis();
            System.out.println("LAPSE: " + (end - start));
        }

        // Test getResultList and parameters
        list = dao.selectByNameAndEMail(cont2.getName(),cont2.getEmail());
        testGetResultList(list,cont2);
        list = dao.selectByNameAndEMail2(cont2.getName(),cont2.getEmail());
        testGetResultList(list,cont2);
        list = dao.selectByNameAndEMail3(cont2.getName(),cont2.getEmail());
        testGetResultList(list,cont2);
        list = dao.selectByNameAndEMail4(cont2.getName(),cont2.getEmail());
        testGetResultList(list,cont2);
        list = dao.selectByNameAndEMail5(cont2.getName(),cont2.getEmail());
        testGetResultList(list,cont2);

        JEPLDAOQuery<Contact> query = dao.getJEPLDAO().createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = :name AND EMAIL = ?2");
        assertFalse(query.isBound(query.getJEPLParameter("name")));
        assertFalse(query.isBound(query.getJEPLParameter(2)));
        query.setParameter("name",cont2.getName()).setParameter(2,cont2.getEmail());
        assertTrue(query.isBound(query.getJEPLParameter("name")));
        assertTrue(query.isBound(query.getJEPLParameter(2)));
        assertTrue(query.getParameterValue("name").equals(cont2.getName()));
        assertTrue(query.getParameterValue(2).equals(cont2.getEmail()));
        assertTrue(query.getParameterValue(query.getJEPLParameter("name")).equals(cont2.getName()));
        assertTrue(query.getParameterValue(query.getJEPLParameter(2)).equals(cont2.getEmail()));
        return cont2;
    }

    public static void testGetResultList(List<Contact> list,Contact cont2)
    {
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).getName().equals(cont2.getName()));
        assertTrue(list.get(0).getEmail().equals(cont2.getEmail()));
    }

    public static Contact testGetGeneratedKeyAndGetSingleResult(ContactDAO dao)
    {
        // Test getGeneratedKey
        Contact cont = TestDAOShared.createContact();
        dao.insert(cont);
        assertTrue(cont.getId() != 0);

        // Test getSingleResult y comprobamos el insert anterior
        Contact cont2 = dao.selectById(cont.getId());
        assertTrue(cont2 != null);
        assertTrue(cont.getName().equals(cont2.getName()));
        assertTrue(cont.getPhone().equals(cont2.getPhone()));
        assertTrue(cont.getEmail().equals(cont2.getEmail()));
        
        return cont;
    }

    public static void testStandAloneDAODAL(JEPLDAL dal,JEPLDAO<Contact> dao)
    {
        final boolean[] used = new boolean[1];

        JEPLResultSetDAOListener<Contact> listener = new JEPLResultSetDAOListener<Contact>()
        {
            @Override
            public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
                used[0] = true;
            }

            @Override
            public Contact createObject(JEPLResultSet jrs) throws Exception
            {
                return new Contact();
            }

            @Override
            public void fillObject(Contact obj,JEPLResultSet jrs) throws Exception
            {
                ResultSet rs = jrs.getResultSet();

                obj.setId(rs.getInt("ID"));
                obj.setName(rs.getString("NAME"));
                obj.setPhone(rs.getString("PHONE"));
                obj.setEmail(rs.getString("EMAIL"));
            }
        };
        dao.addJEPLListener(listener); // registered before first use (SQL sentence)

        dal.createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();

        Contact contact = TestDAOShared.createContact();

        int id = dao.createJEPLDALQuery(
	                    "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?, ?, ?)")
	                .addParameters(contact.getEmail(),contact.getName(),contact.getPhone())
	                .getGeneratedKey(int.class);
        contact.setId(id);     

        // Testing listener registered calling addJEPLListener
        used[0] = false;
        Contact contact2 = dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE ID = ?")
                .addParameter(id)
                .getSingleResult();
        assertTrue(used[0]);
        assertTrue(contact2 != null);
        assertTrue(contact.getId() == contact2.getId());
        assertTrue(contact.getName().equals(contact2.getName()));
        assertTrue(contact.getPhone().equals(contact2.getPhone()));
        assertTrue(contact.getEmail().equals(contact2.getEmail()));

        final boolean[] used_other = new boolean[1];

        // Testing listener as parameter
        JEPLResultSetDAOListener<Contact> listener2 = new JEPLResultSetDAOListener<Contact>()
        {
            @Override
            public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
                used_other[0] = true;
            }

            @Override
            public Contact createObject(JEPLResultSet jrs) throws Exception
            {
                return new Contact();
            }

            @Override
            public void fillObject(Contact obj,JEPLResultSet jrs) throws Exception
            {
                ResultSet rs = jrs.getResultSet();

                obj.setId(rs.getInt("ID"));
                obj.setName(rs.getString("NAME"));
                obj.setPhone(rs.getString("PHONE"));
                obj.setEmail(rs.getString("EMAIL"));
            }
        };

        used_other[0] = false;
        contact2 = dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE ID = ?")
                    .addJEPLListener(listener2)
                    .addParameter(id)
                    .getSingleResult();
        assertTrue(used_other[0]);
        assertTrue(contact2 != null);
        assertTrue(contact.getId() == contact2.getId());
        assertTrue(contact.getName().equals(contact2.getName()));
        assertTrue(contact.getPhone().equals(contact2.getPhone()));
        assertTrue(contact.getEmail().equals(contact2.getEmail()));
    }
}
