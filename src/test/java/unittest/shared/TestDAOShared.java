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

import example.dao.CompanyDAO;
import example.dao.ContactDAO;
import example.dao.PersonDAO;
import example.model.Company;
import example.model.Contact;
import example.model.Person;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jmarranz
 */
public class TestDAOShared
{
    public static Contact createContact()
    {
        Contact contact = new Contact();
        contact.setName("A Contact object");
        contact.setPhone("9999999");
        contact.setEmail("contact@world.com");
        return contact;
    }

    public static void checkContact(Contact contact)
    {
        assertTrue("A Contact object".equals(contact.getName()));
        assertTrue("9999999".equals(contact.getPhone()));
        assertTrue("contact@world.com".equals(contact.getEmail()));
    }

    public static Contact insertContact(ContactDAO contactDao)
    {
        Contact contact = createContact();
        contactDao.insert(contact);
        return contact;
    }

    public static Person createPerson()
    {
        Person person = new Person();
        person.setName("A Person object");
        person.setPhone("1111111");
        person.setEmail("hello@world.com");
        person.setAge(20);
        return person;
    }
    
    public static Person insertPerson(PersonDAO personDao)
    {
        Person person = createPerson();
        personDao.insert(person);
        return person;
    }

    public static Company createCompany()
    {
        Company company = new Company();
        company.setName("A Company object");
        company.setPhone("4444444");
        company.setEmail("company@world.com");
        company.setAddress("Some Company Address");
        return company;
    }

    public static Company insertCompany(CompanyDAO companyDao)
    {
        Company company = createCompany();
        companyDao.insert(company);
        return company;
    }
}
