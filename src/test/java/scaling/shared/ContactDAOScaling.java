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
package scaling.shared;

import example.model.Contact;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;

public class ContactDAOScaling 
{
    protected JEPLDAO<Contact> dao;
    
    public ContactDAOScaling(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Contact.class);
    }

    public JEPLDAO<Contact> getJEPLDAO()
    {
        return dao;
    }

    public void insertKeyGenerated(Contact contact)
    {
        int key = dao.createJEPLDALQuery(
                    "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?, ?, ?)")
                .addParameters(contact.getEmail(),contact.getName(),contact.getPhone())
                .getGeneratedKey(int.class);
         contact.setId(key);
    }

    public void insertKeyNotGenerated(Contact contact)
    {
        dao.createJEPLDALQuery(
                    "INSERT INTO CONTACT (ID,EMAIL, NAME, PHONE) VALUES (?, ?, ?, ?)")
                .addParameters(contact.getId(),contact.getEmail(),
                        contact.getName(),contact.getPhone())
                    .executeUpdate();
    }

    public void update(Contact contact)
    {
        dao.createJEPLDALQuery("UPDATE CONTACT SET EMAIL = ?, NAME = ?, PHONE = ? WHERE ID = ?")
                .addParameters(contact.getEmail(),contact.getName(),contact.getPhone(),contact.getId())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public boolean deleteById(int id)
    {
        return dao.createJEPLDALQuery("DELETE FROM CONTACT WHERE ID = ?")
                    .setStrictMinRows(0).setStrictMaxRows(1)
                    .addParameter(id)
                    .executeUpdate() > 0;
    }
}
