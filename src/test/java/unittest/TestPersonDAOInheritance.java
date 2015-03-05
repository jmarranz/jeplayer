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
import jepl.JEPLBootNonJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLNonJTADataSource;
import unittest.shared.TestDAOShared;
import example.loadmanually.DataSourceLoaderManualLoad;
import example.model.Company;
import example.dao.CompanyDAO;
import example.dao.ContactDAO;
import example.model.Person;
import example.dao.PersonDAO;
import jepl.JEPLDataSource;
import example.model.Contact;
import java.util.List;
import javax.sql.DataSource;
import jepl.JEPLTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author jmarranz
 */
public class TestPersonDAOInheritance
{

    public TestPersonDAOInheritance()
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
        try
        {         
            JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
            JEPLNonJTADataSource jds;            
            
            jds = boot.createJEPLNonJTADataSource(ds);
            jds.setDefaultAutoCommit(false);
            operations(new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            jds.setDefaultAutoCommit(true);
            operations(new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            final JEPLDataSource jds2 = jds;
            JEPLTask<Object> task = new JEPLTask<Object>()
            {
                @Override
                public Object exec() throws Exception
                {
                    operations(new PersonDAO(jds2),new CompanyDAO(jds2),new ContactDAO(jds2));
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

    public void operations(PersonDAO personDao,CompanyDAO companyDao,ContactDAO contactDao)
    {
        // Tables empty initialization
        // because delete actions are tricky, doing manually (testing delete later)
        personDao.getJEPLDAO().createJEPLDALQuery("DELETE FROM PERSON").executeUpdate();
        companyDao.getJEPLDAO().createJEPLDALQuery("DELETE FROM COMPANY").executeUpdate();
        contactDao.getJEPLDAO().createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();
        List<Person> listPerson = personDao.selectAll();
        assertTrue(listPerson.isEmpty());
        List<Company> listCompany = companyDao.selectAll();
        assertTrue(listCompany.isEmpty());
        List<Contact> listContact = contactDao.selectAll();
        assertTrue(listContact.isEmpty());

        // Inserting a pure Contact and a Company to check inheritance filtering
        Contact c = TestDAOShared.createContact();
        contactDao.insert(c);
        Company company = TestDAOShared.createCompany();
        companyDao.insert(company);

        // Test insert
        Person person = testInsertPerson(personDao);

        // Test selectById y el insert anterior
        Person person2 = personDao.selectById(person.getId());
        assertTrue(person2 != null);
        assertTrue(person.getName().equals(person2.getName()));
        assertTrue(person.getPhone().equals(person2.getPhone()));
        assertTrue(person.getEmail().equals(person2.getEmail()));
        assertTrue(person.getAge() == person2.getAge());

        // Test selectAll
        person2 = new Person();
        person2.setName("Another Person object");
        person2.setPhone("2222222");
        person2.setEmail("bye@world.com");
        person2.setAge(30);
        personDao.insert(person2);
        listPerson = personDao.selectAll();
        assertTrue(listPerson.size() == 2);
        while(!listPerson.isEmpty())
        {
            int id = listPerson.get(0).getId();
            assertTrue(person.getId() == id || person2.getId() == id);
            listPerson.remove(0);
        }

        // Test selectCount
        int count = personDao.selectCount();
        assertTrue(count == 2);

        // Test selectByNameAndEMail
        listPerson = personDao.selectByNameAndEMail(person2.getName(),person2.getEmail());
        assertTrue(listPerson.size() == 1);
        assertTrue(((Person)listPerson.get(0)).getName().equals(person2.getName()));
        assertTrue(((Person)listPerson.get(0)).getEmail().equals(person2.getEmail()));

        // Test update
        person.setName("A Person object CHANGED");
        personDao.update(person);
        person2 = personDao.selectById(person.getId());
        assertTrue(person2.getName().equals("A Person object CHANGED"));

        // Test deleteByIdCascade
        person = personDao.selectById(person.getId());
        assertTrue(person != null);        
        boolean deleted = personDao.deleteByIdCascade(person.getId());
        if (!DataSourceLoaderManualLoad.android)                
        	assertTrue(deleted);
        person = personDao.selectById(person.getId());
        assertTrue(person == null);

        // Test deleteByIdNotCascade1
        person = testInsertPerson(personDao);
        person = personDao.selectById(person.getId());
        assertTrue(person != null);          
        deleted = personDao.deleteByIdNotCascade1(person.getId());
        if (!DataSourceLoaderManualLoad.android)                
        	assertTrue(deleted);
        person = personDao.selectById(person.getId());
        assertTrue(person == null);
        
        // Test deleteByIdNotCascade2 (MySQL only)
        if (!DataSourceLoaderManualLoad.android)
        {        
	        person = testInsertPerson(personDao);
	        deleted = personDao.deleteByIdNotCascade2(person.getId()); // MySQL only
	        assertTrue(deleted);
	        person = personDao.selectById(person.getId());
	        assertTrue(person == null);
        }
        
        // Test deleteAllCascade (remains one Person)
        listPerson = personDao.selectAll();
        assertFalse(listPerson.isEmpty());
        personDao.deleteAllCascade();
        listPerson = personDao.selectAll();
        assertTrue(listPerson.isEmpty());
        listContact = contactDao.selectAll(); // Check if Contact non Person is also removed
        assertFalse(listContact.isEmpty());

        // Test deleteAllNotCascade (MySQL only)
        if (!DataSourceLoaderManualLoad.android)
        {        
	        person = TestDAOShared.createPerson();
	        personDao.insert(person);
	        listPerson = personDao.selectAll();
	        assertFalse(listPerson.isEmpty());
	        personDao.deleteAllNotCascade();  // MySQL only
	        listPerson = personDao.selectAll();
	        assertTrue(listPerson.isEmpty());
	        listContact = contactDao.selectAll(); // Check if Contact non Person is also removed
	        assertFalse(listContact.isEmpty());
        }

    }

    public Person testInsertPerson(PersonDAO personDao)
    {
        // Test insert
        Person person = TestDAOShared.createPerson();
        personDao.insert(person);
        assertTrue(person.getId() != 0);

        return person;
    }
}
