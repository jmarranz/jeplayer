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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 * @author jmarranz
 */
public class JEPLResultSetBeanInfo
{
    public String[] columnNameArr;
    public Method[] setterArr;

    public JEPLResultSetBeanInfo(Map<String,JEPLPropertyDescriptorImpl> propertyMap,ResultSet rs) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        this.columnNameArr = new String[cols];
        this.setterArr = new Method[cols];
        for (int col = 1; col <= cols; col++)
        {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || columnName.equals(""))
                columnName = rsmd.getColumnName(col);
            this.columnNameArr[col - 1] = columnName;

            String columnNameLow = columnName.toLowerCase();
            JEPLPropertyDescriptorImpl prop = propertyMap.get(columnNameLow);
            if (prop != null)
            {
                Method setter = prop.getWriteMethod();  
                this.setterArr[col - 1] = setter;                
            }
        }
    }
}
