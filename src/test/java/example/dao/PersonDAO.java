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

import java.sql.ResultSet;
import java.util.List;
import example.model.Person;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLTask;

public class PersonDAO
{
    protected ContactDAO contactDAO;
    protected JEPLDAO<Person> dao;
    protected JEPLResultSetDAOListener<Person> rsDAOListener;    
    
    public PersonDAO(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Person.class);
        
        this.rsDAOListener = new JEPLResultSetDAOListener<Person>()
        {    
            @Override
            public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
            }

            @Override
            public Person createObject(JEPLResultSet jrs) throws Exception
            {
                return new Person();
            }

            @Override
            public void fillObject(Person obj,JEPLResultSet jrs) throws Exception
            {
                contactDAO.getJEPLResultSetDAOListener().fillObject(obj, jrs);

                ResultSet rs = jrs.getResultSet();
                obj.setAge(rs.getInt("AGE"));
            }       
        };        
        
        dao.addJEPLListener(rsDAOListener);
        this.contactDAO = new ContactDAO(ds);
    }

    public JEPLDAO<Person> getJEPLDAO()
    {
        return dao;
    }

    public JEPLResultSetDAOListener<Person> getJEPLResultSetDAOListener()
    {
        return rsDAOListener;
    }    


    public void insert(Person obj)
    {
        contactDAO.insert(obj);
        dao.createJEPLDALQuery("INSERT INTO PERSON (ID, AGE) VALUES (?, ?)")
                .addParameters(obj.getId(),obj.getAge())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public void update(Person obj)
    {
        contactDAO.update(obj);
        dao.createJEPLDALQuery("UPDATE PERSON SET AGE = ? WHERE ID = ?")
                .addParameters(obj.getAge(),obj.getId())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public boolean deleteByIdCascade(int id)
    {
        // Only use when ON DELETE CASCADE is defined in foreign keys
        return contactDAO.deleteById(id);
    }

    public boolean deleteByIdNotCascade1(int id)
    {
        boolean res = dao.createJEPLDALQuery("DELETE FROM PERSON WHERE ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
        if (res) contactDAO.deleteById(id);
        return res;
    }
    
    public boolean deleteByIdNotCascade2(int id)
    {
        // Only MySQL
        return dao.createJEPLDALQuery("DELETE C,P FROM CONTACT C " +
                    "LEFT JOIN PERSON P ON C.ID = P.ID " +
                    "WHERE P.ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
    }

    public boolean delete(Person person)
    {
        return deleteByIdCascade(person.getId());
    }

    public int deleteAll()
    {
        return deleteAllCascade();
    }

    public int deleteAllCascade()
    {
        // Only use when ON DELETE CASCADE is defined in foreign key
        return dao.createJEPLDALQuery("DELETE FROM CONTACT WHERE CONTACT.ID IN (SELECT ID FROM PERSON)").executeUpdate();
    }

    public int deleteAllNotCascade()
    {
        // MySQL Only
        return dao.createJEPLDALQuery("DELETE CONTACT,PERSON FROM CONTACT INNER JOIN PERSON WHERE CONTACT.ID = PERSON.ID").executeUpdate();
    }

    public List<Person> selectAll()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID")
                .getResultList();
    }

    public List<Person> selectAllOrderById()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID ORDER BY P.ID")
                .getResultList();
    }

    public Person selectById(int id)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID AND P.ID = ?")
                .addParameter(id)
                .getSingleResult();
    }

    public List<Person> selectByNameAndEMail(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID AND C.NAME = ? AND C.EMAIL = ?")
                .addParameters(name,email)
                .getResultList();
    }

    public int selectCount()
    {
        return dao.createJEPLDALQuery("SELECT COUNT(*) FROM PERSON")
                    .getOneRowFromSingleField(int.class);
    }
}
