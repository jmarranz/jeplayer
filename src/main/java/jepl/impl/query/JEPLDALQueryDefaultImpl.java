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

package jepl.impl.query;

import jepl.JEPLDALQuery;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLResultSetDAOListener;
import jepl.JEPLUpdateDAOListener;
import jepl.impl.JEPLDALImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLDALQueryDefaultImpl extends JEPLDALQueryImpl
{   
    public JEPLDALQueryDefaultImpl(JEPLDALImpl dal,String sqlOriginal)
    {
        super(dal,sqlOriginal);
        
        parseSQL();        
    }

    @Override
    public JEPLDALQuery addJEPLListener(JEPLListener listener)
    {
        if (listener instanceof JEPLResultSetDAOListener || listener instanceof JEPLUpdateDAOListener)
            throw new JEPLException("You cannot register a DAO listener in this level"); // Porque sólo se permite uno de cada tipo de listener y clases-modelo hay varias
        
        return super.addJEPLListener(listener);
    }    
}
