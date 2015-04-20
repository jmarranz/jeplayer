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

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import jepl.impl.JEPLConnectionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLUpdateBeanInfo
{
    public ArrayList<String> columnNameArr;
    public Method[] getterArr;

    public JEPLUpdateBeanInfo(JEPLConnectionImpl jcon,String tableName,Map<String,JEPLPropertyDescriptorImpl> propertyMap) throws SQLException
    {
        // http://tutorials.jenkov.com/jdbc/databasemetadata.html#listing-columns-in-a-table
        // http://www.herongyang.com/JDBC/sqljdbc-jar-Column-List.html
        
        DatabaseMetaData dbMetaData =  jcon.getConnection().getMetaData();
        
        String   catalog           = null;
        String   schemaPattern     = null;
        String   columnNamePattern = null;

        ResultSet result = dbMetaData.getColumns(catalog, schemaPattern,  tableName, columnNamePattern);

        ArrayList<String> columnNameArr = new ArrayList<String>();
        while(result.next())
        {
            String columnName = result.getString("COLUMN_NAME");
            columnNameArr.add(columnName);
        }              
        result.close();
        
        int cols = columnNameArr.size();

        this.getterArr = new Method[cols];
        for (int col = 0; col < cols; col++)
        {
            String columnName = columnNameArr.get(col);
            String columnNameLow = columnName.toLowerCase();
            JEPLPropertyDescriptorImpl prop = propertyMap.get(columnNameLow);
            if (prop != null)
            {
                Method getter = prop.getReadMethod();  
                this.getterArr[col] = getter;
            }
        }
    }
}
