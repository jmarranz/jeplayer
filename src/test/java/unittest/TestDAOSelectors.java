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
import example.dao.ContactDAOSelectorBase;
import example.dao.PersonDAO;
import example.model.Company;
import example.model.Contact;
import example.model.Person;
import javax.sql.DataSource;
import jepl.*;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import unittest.shared.TestDAOShared;


/**
 *
 * @author jmarranz
 */
public class TestDAOSelectors
{

    public TestDAOSelectors()
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
            operations(jds,new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            jds.setDefaultAutoCommit(true);
            operations(jds,new PersonDAO(jds),new CompanyDAO(jds),new ContactDAO(jds));

            jds = boot.createJEPLNonJTADataSource(ds);
            final JEPLDataSource jds2 = jds;
            JEPLTask<Object> task = new JEPLTask<Object>()
            {
                @Override
                public Object exec() throws Exception
                {
                    operations(jds2,new PersonDAO(jds2),new CompanyDAO(jds2),new ContactDAO(jds2));
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

    public void operations(JEPLDataSource jds,PersonDAO personDao,CompanyDAO companyDao,ContactDAO contactDao)
    {
        // Tables empty initialization
        // because delete actions are tricky, doing manually (testing delete later)
        JEPLDAL dal = jds.createJEPLDAL();

        dal.createJEPLDALQuery("DELETE FROM PERSON").executeUpdate();
        dal.createJEPLDALQuery("DELETE FROM COMPANY").executeUpdate();
        dal.createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();


        // Inserting a Contact a Person and a Company

        {
            Contact contact = TestDAOShared.createContact();
            Contact contact2;
            ContactDAOSelectorBase contactDAOSingle = ContactDAOSelectorBase.createContactDAOSelectorBase(contact,jds);

            // Test ContactDAOSelectorBase insert
            contactDAOSingle.insert();
            contact2 = contactDao.selectById(contact.getId());
            assertTrue(contact2 instanceof Contact);
            assertTrue(!(contact2 instanceof Company));
            assertTrue(!(contact2 instanceof Person));

            // Test ContactDAOSelectorBase update
            contact.setName("A Contact object CHANGED");
            contactDAOSingle.update();
            contact2 = contactDao.selectById(contact.getId());
            assertTrue(contact2.getName().equals("A Contact object CHANGED"));

            // Test ContactDAOSelectorBase delete
            contactDAOSingle.delete();
            contact2 = contactDao.selectById(contact.getId());
            assertTrue(contact2 == null);
        }

        {
            Person person = TestDAOShared.createPerson();
            Person person2;
            ContactDAOSelectorBase personDAOSingle = ContactDAOSelectorBase.createContactDAOSelectorBase(person,jds);

            // Test ContactDAOSelectorBase insert
            personDAOSingle.insert();
            person2 = personDao.selectById(person.getId());
            assertTrue(person2 instanceof Person);

            // Test ContactDAOSelectorBase update
            person.setName("A Person object CHANGED");
            personDAOSingle.update();
            person2 = personDao.selectById(person.getId());
            assertTrue(person2.getName().equals("A Person object CHANGED"));

            // Test ContactDAOSelectorBase delete
            personDAOSingle.delete();
            person2 = personDao.selectById(person.getId());
            assertTrue(person2 == null);
        }

        {

            Company company = TestDAOShared.createCompany();
            Company company2;
            ContactDAOSelectorBase companyDAOSingle = ContactDAOSelectorBase.createContactDAOSelectorBase(company,jds);            

            // Test ContactDAOSelectorBase insert            
            companyDAOSingle.insert();
            company2 = companyDao.selectById(company.getId());
            assertTrue(company2 instanceof Company);            
            
            // Test ContactDAOSelectorBase update            
            company.setName("A Company object CHANGED");
            companyDAOSingle.update();
            company2 = companyDao.selectById(company.getId());
            assertTrue(company2.getName().equals("A Company object CHANGED"));

            // Test ContactDAOSelectorBase delete
            companyDAOSingle.delete();
            company2 = companyDao.selectById(company.getId());
            assertTrue(company2 == null);            
        }

    }


}
