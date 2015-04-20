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

import jepl.impl.query.JEPLDAOQueryImpl;
import jepl.JEPLDAO;
import jepl.JEPLDAOQuery;

/**
 * This class is designed to inherit end user classes to provide DAO based services.
 *
 * @author jmarranz
 * @param <T>
 */
public class JEPLDAOImpl<T> extends JEPLDALImpl implements JEPLDAO<T>
{
    public JEPLDAOImpl(JEPLDataSourceImpl ds)
    {
        super(ds);
    }

    @Override
    public JEPLDAOQuery<T> createJEPLDAOQuery(String sql)
    {
        return new JEPLDAOQueryImpl<T>(this,sql);
    }

    @Override
    public JEPLDAOQuery<T> insert()
    {

        JEPLDAOQueryImpl<T> query = new JEPLDAOQueryImpl<T>(this,"");
        return query;
    }    
}
