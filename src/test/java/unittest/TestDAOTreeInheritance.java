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
import example.dao.CompanyDAO;
import example.dao.ContactDAO;
import example.dao.ContactTreeDAO;
import example.dao.PersonDAO;
import example.loadmanually.DataSourceLoaderManualLoad;
import example.model.Company;
import example.model.Contact;
import example.model.Person;
import java.util.List;
import javax.sql.DataSource;
import jepl.*;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import unittest.shared.TestDAOShared;


/**
 *
 * @author jmarranz
 */
public class TestDAOTreeInheritance
{

    public TestDAOTreeInheritance()
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
                
                DataSource ds = dsFactory.getDataSource();
                CreateDBModel.createDB(ds);                
                
                execTest(ds);
            }
            finally
            {
                dsFactory.destroy();
            }
        }
    }

    public void execTest(DataSource ds)
    {
        JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
        JEPLNonJTADataSource jds;

        try
        {
            jds = boot.createJEPLNonJTADataSource(ds);
            jds.setDefaultAutoCommit(false);
            operations(new ContactTreeDAO(jds),new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            jds.setDefaultAutoCommit(true);
            operations(new ContactTreeDAO(jds),new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            final JEPLDataSource jds2 = jds;
            JEPLTask<Object> task = new JEPLTask<Object>()
            {
                @Override
                public Object exec() throws Exception
                {
                    operations(new ContactTreeDAO(jds2),new PersonDAO(jds2),new CompanyDAO(jds2),new ContactDAO(jds2));
                    return null;
                }
            };
            jds.exec(task,true);  // No transaction
            jds.exec(task,false); // Transaction
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

    public void operations(ContactTreeDAO contactDAOMult,PersonDAO personDao,CompanyDAO companyDao,ContactDAO contactDao)
    {
        // Tables empty initialization
        // because delete actions are tricky, doing manually (testing delete later)
        contactDAOMult.getJEPLDAO().createJEPLDALQuery("DELETE FROM PERSON").executeUpdate();
        contactDAOMult.getJEPLDAO().createJEPLDALQuery("DELETE FROM COMPANY").executeUpdate();
        contactDAOMult.getJEPLDAO().createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();

        // Test ContactTreeDAO selectAll
        List<Contact> list = contactDAOMult.selectAll();
        assertTrue(list.isEmpty());

        Contact contact = TestDAOShared.insertContact(contactDao);
        Person person = TestDAOShared.insertPerson(personDao);
        Company company = TestDAOShared.insertCompany(companyDao);

        list = contactDAOMult.selectAll();
        assertTrue(list.size() == 3);
        assertTrue(list.get(0).getClass().equals(Contact.class));
        assertTrue(list.get(1).getClass().equals(Person.class));
        assertTrue(list.get(2).getClass().equals(Company.class));

        // Test ContactTreeDAO selectById
        Contact contact3 = contactDAOMult.selectById(contact.getId());
        assertTrue(contact3 != null);
        assertTrue(contact3.getClass().equals(Contact.class));
        assertTrue(contact3.getName().equals(contact.getName()));
        assertTrue(contact3.getPhone().equals(contact.getPhone()));
        assertTrue(contact3.getEmail().equals(contact.getEmail()));
        
        Contact person3 = contactDAOMult.selectById(person.getId());
        assertTrue(person3 != null);
        assertTrue(person3.getClass().equals(Person.class));
        assertTrue(person3.getName().equals(person.getName()));
        assertTrue(person3.getPhone().equals(person.getPhone()));
        assertTrue(person3.getEmail().equals(person.getEmail()));
        assertTrue(((Person)person3).getAge() == person.getAge());

        Contact company3 = contactDAOMult.selectById(company.getId());
        assertTrue(company3 != null);
        assertTrue(company3.getClass().equals(Company.class));
        assertTrue(company3.getName().equals(company.getName()));
        assertTrue(company3.getPhone().equals(company.getPhone()));
        assertTrue(company3.getEmail().equals(company.getEmail()));
        assertTrue(((Company)company3).getAddress().equals(company.getAddress()));

        // Test ContactTreeDAO.deleteAllCascade
        list = contactDAOMult.selectAll();
        assertTrue(list.size() == 3);        
        int count = contactDAOMult.deleteAllCascade();
        if (!DataSourceLoaderManualLoad.android) // SQLDroid devuelve siempre 0
        	assertTrue(count == 3);
        list = contactDAOMult.selectAll();
        assertTrue(list.isEmpty());
        
        // Test ContactTreeDAO.deleteAllNotCascade (MySQL Only)
        if (!DataSourceLoaderManualLoad.android)
        {
	        contact = TestDAOShared.insertContact(contactDao);
	        person = TestDAOShared.insertPerson(personDao);
	        company = TestDAOShared.insertCompany(companyDao);
	        count = contactDAOMult.deleteAllNotCascade(); // MySQL Only
	        assertTrue(count == 3);
	        list = contactDAOMult.selectAll();
	        assertTrue(list.isEmpty());
        }
        
        // Test ContactTreeDAO.deleteByIdNotCascade (MySQL Only)
        if (!DataSourceLoaderManualLoad.android)
        {        
	        contact = TestDAOShared.insertContact(contactDao);
	        person = TestDAOShared.insertPerson(personDao);
	        company = TestDAOShared.insertCompany(companyDao);
	        boolean deleted;
	        deleted = contactDAOMult.deleteByIdNotCascade(contact.getId()); // MySQL Only
	        assertTrue(deleted);
	        deleted = contactDAOMult.deleteByIdNotCascade(person.getId());
	        assertTrue(deleted);
	        deleted = contactDAOMult.deleteByIdNotCascade(company.getId());
	        assertTrue(deleted);
	        list = contactDAOMult.selectAll();
	        assertTrue(list.isEmpty());
        }
    }

}
