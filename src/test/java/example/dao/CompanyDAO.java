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
import java.sql.ResultSet;
import java.util.List;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLTask;

public class CompanyDAO implements JEPLResultSetDAOListener<Company>
{
    protected ContactDAO contactDAO;
    protected JEPLDAO<Company> dao;

    public CompanyDAO(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Company.class);
        dao.addJEPLListener(this);
        this.contactDAO = new ContactDAO(ds);
    }

    public JEPLDAO<Company> getJEPLDAO()
    {
        return dao;
    }

    @Override
    public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
    {
    }
    
    @Override
    public Company createObject(JEPLResultSet jrs) throws Exception
    {
        return new Company();
    }

    @Override
    public void fillObject(Company obj,JEPLResultSet jrs) throws Exception
    {
        contactDAO.fillObject(obj, jrs);
        ResultSet rs = jrs.getResultSet();
        obj.setAddress(rs.getString("ADDRESS"));
    }

    public void insert(Company obj)
    {
        contactDAO.insert(obj);
        dao.createJEPLDALQuery("INSERT INTO COMPANY (ID, ADDRESS) VALUES (?, ?)")
                .addParameters( obj.getId(),obj.getAddress())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public void update(Company obj)
    {
        contactDAO.update(obj);
        dao.createJEPLDALQuery("UPDATE COMPANY SET ADDRESS = ? WHERE ID = ?")
                .addParameters(obj.getAddress(),obj.getId())
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
        boolean res = dao.createJEPLDALQuery("DELETE FROM COMPANY WHERE ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
        if (res) contactDAO.deleteById(id);
        return res;
    }

    public boolean deleteByIdNotCascade2(int id)
    {
        // Only MySQL      
        return dao.createJEPLDALQuery("DELETE C,CP FROM CONTACT C " +
                    "LEFT JOIN PERSON CP ON C.ID = CP.ID " +
                    "WHERE CP.ID = ?")
                .setStrictMinRows(0).setStrictMaxRows(1)
                .addParameter(id)
                .executeUpdate() > 0;
    }

    public boolean delete(Company company)
    {
        return deleteByIdCascade(company.getId());
    }

    public int deleteAll()
    {
        return deleteAllCascade();
    }

    public int deleteAllCascade()
    {
        // Only use when ON DELETE CASCADE is defined in foreign key
        return dao.createJEPLDALQuery("DELETE FROM CONTACT WHERE CONTACT.ID IN (SELECT ID FROM COMPANY)").executeUpdate();
    }

    public int deleteAllNotCascade()
    {
        // MySQL Only
        return dao.createJEPLDALQuery("DELETE CONTACT,COMPANY FROM CONTACT INNER JOIN COMPANY WHERE CONTACT.ID = COMPANY.ID").executeUpdate();
    }

    public List<Company> selectAll()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM COMPANY P,CONTACT C WHERE P.ID = C.ID")
                .getResultList();
    }

    public Company selectById(int id)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM COMPANY CP,CONTACT C WHERE CP.ID = C.ID AND CP.ID = ?")
                .addParameter(id)
                .getSingleResult();
    }

    public List<Company> selectByNameAndEMail(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM COMPANY CP,CONTACT C WHERE CP.ID = C.ID AND C.NAME = ? AND C.EMAIL = ?")
                .addParameters(name,email)
                .getResultList();
    }

    public int selectCount()
    {
        return dao.createJEPLDALQuery("SELECT COUNT(*) FROM COMPANY")
                    .getOneRowFromSingleField(int.class);
    }
}
