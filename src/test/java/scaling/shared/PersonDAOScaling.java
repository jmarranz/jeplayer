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

import example.model.Person;
import java.util.List;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;

public class PersonDAOScaling
{
    protected ContactDAOScaling contactDAO;
    protected JEPLDAO<Person> dao;
    
    public PersonDAOScaling(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Person.class);
        dao.addJEPLListener(ds.createJEPLResultSetDAOListenerDefault(Person.class));
        this.contactDAO = new ContactDAOScaling(ds);
    }

    public void insertKeyGenerated(Person obj)
    {
        contactDAO.insertKeyGenerated(obj);
        dao.createJEPLDALQuery("INSERT INTO PERSON (ID, AGE) VALUES (?, ?)")
                .addParameters(obj.getId(),obj.getAge())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public void insertKeyNotGenerated(Person obj)
    {
        contactDAO.insertKeyNotGenerated(obj);
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

    public boolean deleteById(int id)
    {
        boolean res = dao.createJEPLDALQuery("DELETE FROM PERSON WHERE ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
        if (res) contactDAO.deleteById(id);
        return res;
    }

    public List<Person> selectRangeOrderByIdDesc(int start,int end)
    {
        // Ordered by ID descending to force getting ever new inserted rows when start is 0
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID ORDER BY P.ID DESC")
                .setFirstResult(start + 1)
                .setMaxResults(end - start)
                .getResultList();
    }


    public List<Person> selectAllOrderById()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM PERSON P,CONTACT C WHERE P.ID = C.ID ORDER BY P.ID")
                .getResultList();
    }

    public int selectCount()
    {
        return dao.createJEPLDALQuery("SELECT COUNT(*) FROM COMPANY")
                    .getOneRowFromSingleField(int.class);
    }
    
}
