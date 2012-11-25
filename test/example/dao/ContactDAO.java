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

import example.model.Contact;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLPreparedStatement;
import jepl.JEPLPreparedStatementListener;
import jepl.JEPLResultSet;
import jepl.JEPLResultSetDALListener;
import jepl.JEPLResultSetDAO;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLResultSetDAOListenerDefault;
import jepl.JEPLRowBeanMapper;
import jepl.JEPLTask;

public class ContactDAO implements JEPLResultSetDAOListener<Contact>
{
    protected JEPLDAO<Contact> dao;
    
    public ContactDAO(JEPLDataSource ds)
    {
        this.dao = ds.createJEPLDAO(Contact.class);
        dao.addJEPLListener(this);
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

    public void insert(Contact contact)
    {
        int key = dao.createJEPLDALQuery(
                    "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?, ?, ?)")
                .addParameters(contact.getEmail(),contact.getName(),contact.getPhone())
                .getGeneratedKey(int.class);
         contact.setId(key);
    }

    public void insertExplicitResultSetListener(Contact contact)
    {
        JEPLResultSetDALListener listener = new JEPLResultSetDALListener()
        {
            @Override
            public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
            }
            @Override
            @SuppressWarnings("unchecked")
            public <U> U getValue(int columnIndex, Class<U> returnType, JEPLResultSet jrs) throws Exception
            {
                if (!returnType.equals(int.class)) throw new RuntimeException("UNEXPECTED");
                // Expected columnIndex = 1
                int resInt = jrs.getResultSet().getInt(columnIndex);
                Object resObj = jrs.getResultSet().getObject(columnIndex);
                Integer resIntObj = (Integer)jrs.getJEPLStatement().getJEPLDAL().cast(resObj, returnType);
                if (resInt != resIntObj) throw new RuntimeException("UNEXPECTED");
                return (U)resIntObj; 
            }
        };

        int key = dao.createJEPLDALQuery(
                    "INSERT INTO CONTACT (EMAIL, NAME, PHONE) VALUES (?, ?, ?)")
                    .addParameters(contact.getEmail(),contact.getName(),contact.getPhone())
                    .addJEPLListener(listener)
                    .getGeneratedKey(int.class);
         contact.setId(key);
    }
    
    public void update(Contact contact)
    {
        dao.createJEPLDALQuery("UPDATE CONTACT SET EMAIL = ?, NAME = ?, PHONE = ? WHERE ID = ?")
                .addParameters(contact.getEmail(),contact.getName(),contact.getPhone(),contact.getId())
                .setStrictMinRows(1).setStrictMaxRows(1)
                .executeUpdate();
    }

    public boolean delete(Contact obj)
    {
        return deleteById(obj.getId());
    }

    public boolean deleteById(int id)
    {
        // Only if there is no "inherited" rows or declared ON DELETE CASCADE
        return dao.createJEPLDALQuery("DELETE FROM CONTACT WHERE ID = ?")
                    .setStrictMinRows(0).setStrictMaxRows(1)
                    .addParameter(id)
                    .executeUpdate() > 0;
    }

    public int deleteAll()
    {
        // Only if "inherited" tables are empty or declared ON DELETE CASCADE
        return dao.createJEPLDALQuery("DELETE FROM CONTACT").executeUpdate();
    }

    public List<Contact> selectAll()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT").getResultList();
    }

    public JEPLResultSetDAO<Contact> selectAllResultSetDAO()
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT").getJEPLResultSetDAO();
    }

    public List<Contact> selectAllExplicitResultSetListener()
    {
        JEPLResultSetDAOListener<Contact> listener = new JEPLResultSetDAOListener<Contact>()
        {
            @Override
            public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
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

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }

    public List<Contact> selectAllExplicitResultSetDAOListenerBean()
    {
        JEPLResultSetDAOListenerDefault<Contact> listener =
                dao.getJEPLDataSource().createJEPLResultSetDAOListenerDefault(Contact.class);

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }

    public List<Contact> selectAllExplicitResultSetDAOListenerBeanWithMapper()
    {
        JEPLRowBeanMapper<Contact> rowMapper = new JEPLRowBeanMapper<Contact>()
        {
            public boolean setColumnInBean(Contact obj,JEPLResultSet jrs, int col, String columnName, Object value, Method setter)
            {
                if (columnName.equalsIgnoreCase("email"))
                {
                    obj.setEmail((String)value);
                    return true;
                }
                return false;
            }
        };
        JEPLResultSetDAOListenerDefault<Contact> listener =
                dao.getJEPLDataSource().createJEPLResultSetDAOListenerDefault(Contact.class,rowMapper);

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }

    public List<Contact> selectJEPLDAOQueryRange(int from,int to)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .setFirstResult(from)
                .setMaxResults(to - from)
                .getResultList();
    }

    public List<Contact> selectAllExplicitResultSetListenerRange(final int from,final int to)
    {
        JEPLResultSetDAOListener<Contact> listener = new JEPLResultSetDAOListener<Contact>()
        {
            @Override
            public  void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
                ResultSet rs = jrs.getResultSet();
                rs.absolute(from);

                // Not needed, just to know the final state of ResultSet:
                @SuppressWarnings("unchecked")
                List<Contact> res = (List<Contact>)task.exec(); 
                if (res.size() != to - from) throw new RuntimeException("UNEXPECTED");

                int row = rs.getRow(); // ResultSet is not closed yet
                if (row + 1 != to) throw new RuntimeException("UNEXPECTED");
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
                int row = rs.getRow(); 
                if (row + 1 == to)
                    jrs.stop();
            }
        };

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }

    public List<Contact> selectAllExplicitResultSetListenerRange2(final int from,final int to)
    {
        class RangeDAOListener<T> implements JEPLResultSetDAOListener<T>
        {
            protected int from, to;
            protected JEPLResultSetDAOListener<T> parent;

            public RangeDAOListener(int from,int to,JEPLResultSetDAOListener<T> parent)
            {
                this.from = from;
                this.to = to;
                this.parent = parent;
            }
            @Override
            public  void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
            {
                parent.setupJEPLResultSet(jrs, task);
                jrs.getResultSet().absolute(from);
            }

            @Override
            public T createObject(JEPLResultSet jrs) throws Exception
            {
                return parent.createObject(jrs);
            }

            @Override
            public void fillObject(T obj,JEPLResultSet jrs) throws Exception
            {
                parent.fillObject(obj, jrs);
                int row = jrs.getResultSet().getRow(); // Now returned value is the "next row"
                if (row + 1 == to)
                    jrs.stop();
            }
        }
        
        RangeDAOListener<Contact> listener = new RangeDAOListener<Contact>(from,to,this);

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }

    public List<Contact> selectAllStatementListenerMaxRows(final int maxRows)
    {
        JEPLPreparedStatementListener<List<Contact>> listener =
                new JEPLPreparedStatementListener<List<Contact>>()
        {
            public void setupJEPLPreparedStatement(JEPLPreparedStatement jstmt,
                    JEPLTask<List<Contact>> task) throws Exception
            {
                PreparedStatement stmt = jstmt.getPreparedStatement();
                int old = stmt.getMaxRows();
                stmt.setMaxRows(maxRows);
                try
                {
                    List<Contact> res = task.exec();
                }
                finally
                {
                    stmt.setMaxRows(old); // Restore
                }
            }
        };

        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT")
                .addJEPLListener(listener)
                .getResultList();
    }


    public Contact selectById(int id)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE ID = ?")
                .addParameter(id)
                .getSingleResult();
    }

    public List<Contact> selectByNameAndEMail(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = ? AND EMAIL = ?")
                .addParameters(name,email)
                .getResultList();
    }

    public List<Contact> selectByNameAndEMail2(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = ?1 AND EMAIL = ?2")
                .addParameters(name,email)
                .getResultList();
    }

    public List<Contact> selectByNameAndEMail3(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = ? AND EMAIL = ?2")
                .setParameter(1,name).setParameter(2,email)
                .getResultList();
    }

    public List<Contact> selectByNameAndEMail4(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = :name AND EMAIL = :email")
                .setParameter("name",name).setParameter("email",email)
                .getResultList();
    }

    public List<Contact> selectByNameAndEMail5(String name,String email)
    {
        return dao.createJEPLDAOQuery("SELECT * FROM CONTACT WHERE NAME = :name AND EMAIL = ?2")
                .setParameter("name",name).setParameter(2,email)
                .getResultList();
    }


    public int selectCount()
    {
        return dao.createJEPLDALQuery("SELECT COUNT(*) FROM CONTACT")
                    .getOneRowFromSingleField(int.class);
    }
}
