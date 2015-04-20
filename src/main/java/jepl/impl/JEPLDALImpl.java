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
package jepl.impl;

import jepl.impl.query.JEPLDALQueryImpl;
import java.sql.SQLException;
import java.sql.Statement;
import jepl.JEPLDAL;
import jepl.JEPLDAO;
import jepl.JEPLDataSource;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.impl.query.JEPLDALQueryDefaultImpl;

/**
 * This class is designed to inherit end user classes, usually from {@link JEPLDAO}
 * to provide DAO based services.
 *
 * @author jmarranz
 */
public abstract class JEPLDALImpl implements JEPLDAL
{
    protected JEPLDataSourceImpl jds;
    protected JEPLListenerListImpl listenerList = new JEPLListenerListImpl();
    protected JEPLUserDataMultiThreadImpl userData = new JEPLUserDataMultiThreadImpl();

    public JEPLDALImpl(JEPLDataSourceImpl jds)
    {
        if (jds == null) throw new JEPLException("JEPLDataSource cannot be null");
        this.jds = jds;
    }

    @Override
    public JEPLDataSource getJEPLDataSource()
    {
        return getJEPLDataSourceImpl();
    }

    public JEPLDataSourceImpl getJEPLDataSourceImpl()
    {
        return jds;
    }

    @Override
    public String[] getUserDataNames()
    {
        return userData.getUserDataNames();
    }

    @Override
    public boolean containsName(String name)
    {
        return userData.containsName(name);
    }

    @Override
    public Object getUserData(String name)
    {
        return userData.getUserData(name);
    }

    @Override
    public <T> T getUserData(String name, Class<T> returnType)
    {
        return userData.getUserData(name, returnType);
    }

    @Override
    public Object setUserData(String name, Object value)
    {
        return userData.setUserData(name, value);
    }

    @Override
    public Object removeUserData(String name)
    {
        return userData.removeUserData(name);
    }

    @Override
    public <T> T removeUserData(String name, Class<T> returnType)
    {
        return userData.removeUserData(name, returnType);
    }

    public JEPLListenerListImpl getJEPLListenerList()
    {
        return listenerList;
    }

    public JEPLPreparedStatementImpl createJEPLPrepareStatement(JEPLConnectionImpl conWrap,boolean generatedKeys,String sqlJDBC,Object[] paramList) throws SQLException
    {
        listenerList.setInUse(); // Si creamos un statement estamos usando ya este JEPLDAL/DAO

        JEPLPreparedStatementImpl stmt = conWrap.prepareJEPLStatement(this,sqlJDBC,
                generatedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        if (paramList != null) stmt.setParameters(paramList);

        return stmt;
    }

    @Override
    public JEPLDALQueryImpl createJEPLDALQuery(String sql)
    {
        return new JEPLDALQueryDefaultImpl(this,sql);
    }

    @Override
    public <U> U cast(Object obj,Class<U> returnType)
    {
        return JEPLUtilImpl.cast(obj, returnType);
    }

    @Override
    public void addJEPLListener(JEPLListener listener)
    {
        getJEPLListenerList().addJEPLListener(listener);
    }

    @Override
    public void removeJEPLListener(JEPLListener listener)
    {
        getJEPLListenerList().removeJEPLListener(listener);
    }
}
