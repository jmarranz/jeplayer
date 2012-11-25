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
package example.dao;

import example.model.Company;
import example.model.Contact;
import example.model.Person;
import java.sql.ResultSet;
import java.util.List;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLTask;

public class ContactTreeDAO implements JEPLResultSetDAOListener<Contact>
{
    protected JEPLDAO<Contact> dao;
    protected ContactDAO contactDAO;
    protected PersonDAO personDAO;
    protected CompanyDAO companyDAO;

    public ContactTreeDAO(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Contact.class);
        dao.addJEPLListener(this);
        this.contactDAO = new ContactDAO(ds);
        this.personDAO = new PersonDAO(ds);
        this.companyDAO = new CompanyDAO(ds);
    }

    public JEPLDAO<Contact> getJEPLDAO()
    {
        return dao;
    }
    
    @Override
    public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
    {
    }

    @Override
    public Contact createObject(JEPLResultSet jrs) throws Exception
    {
        ResultSet rs = jrs.getResultSet();
        if (rs.getObject("P_ID") != null)
            return new Person();
        else if(rs.getObject("CP_ID") != null)
            return new Company();
        return new Contact();
    }

    @Override
    public void fillObject(Contact obj,JEPLResultSet jrs) throws Exception
    {
        if (obj instanceof Person)
            personDAO.fillObject((Person)obj, jrs);
        else if(obj instanceof Company)
            companyDAO.fillObject((Company)obj, jrs);
        else // Contact
            contactDAO.fillObject(obj, jrs);
    }

    public int deleteAll()
    {
        return deleteAllCascade();
    }

    public int deleteAllCascade()
    {
        // Only use when ON DELETE CASCADE is defined in foreign keys
        return dao.createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();
    }

    public int deleteAllNotCascade()
    {
        // MySQL Only
        // http://www.haughin.com/2007/11/01/mysql-delete-across-multiple-tables-using-join/
        return dao.createJEPLDALQuery("DELETE C,P,CP FROM CONTACT C " +
                "LEFT JOIN PERSON P ON C.ID = P.ID " +
                "LEFT JOIN COMPANY CP ON C.ID = CP.ID ").executeUpdate();
    }

    public boolean deleteByIdNotCascade(int id)
    {
        // MySQL Only
        // http://www.haughin.com/2007/11/01/mysql-delete-across-multiple-tables-using-join/
        return dao.createJEPLDALQuery("DELETE C,P,CP FROM CONTACT C " +
                "LEFT JOIN PERSON P ON C.ID = P.ID " +
                "LEFT JOIN COMPANY CP ON C.ID = CP.ID " +
                "WHERE C.ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
    }

    public List<Contact> selectAll()
    {
        return dao.createJEPLDAOQuery("SELECT C.ID,C.EMAIL,C.NAME,C.PHONE,P.ID AS P_ID,P.AGE,CP.ID AS CP_ID,CP.ADDRESS " +
                "FROM CONTACT C " +
                "LEFT JOIN PERSON P ON C.ID = P.ID " +
                "LEFT JOIN COMPANY CP ON C.ID = CP.ID")
                .getResultList();
    }

    public Contact selectById(int id)
    {
        return dao.createJEPLDAOQuery("SELECT C.ID,C.EMAIL,C.NAME,C.PHONE,P.ID AS P_ID,P.AGE,CP.ID AS CP_ID,CP.ADDRESS " +
                "FROM CONTACT C " +
                "LEFT JOIN PERSON P ON C.ID = P.ID " +
                "LEFT JOIN COMPANY CP ON C.ID = CP.ID " +
                "WHERE C.ID = ?")
                .addParameter(id)
                .getSingleResult();
    }
}
