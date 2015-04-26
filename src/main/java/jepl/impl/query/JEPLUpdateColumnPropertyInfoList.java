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
public class JEPLUpdateColumnPropertyInfoList
{
    public JEPLUpdateColumnPropertyInfo[] columnArray; 
    
    public JEPLUpdateColumnPropertyInfoList(JEPLConnectionImpl jcon,String tableName,Map<String,JEPLBeanPropertyDescriptorImpl> propertyMap) throws SQLException
    {
        // http://tutorials.jenkov.com/jdbc/databasemetadata.html#listing-columns-in-a-table
        // http://www.herongyang.com/JDBC/sqljdbc-jar-Column-List.html
        
        DatabaseMetaData dbMetaData =  jcon.getConnection().getMetaData();
        
        String   catalog           = null;
        String   schemaPattern     = null;
        String   columnNamePattern = null;

        ResultSet result = dbMetaData.getColumns(catalog, schemaPattern,  tableName, columnNamePattern);

        ArrayList<String> columnNameList = new ArrayList<String>();
        ArrayList<Boolean> autoIncColumnList = new ArrayList<Boolean>();        
        while(result.next())
        {
            String columnName = result.getString("COLUMN_NAME");
            columnNameList.add(columnName);
            autoIncColumnList.add(result.getBoolean("IS_AUTOINCREMENT"));
        }              
        result.close();
        
        int cols = columnNameList.size();
        
        this.columnArray = new JEPLUpdateColumnPropertyInfo[cols];
        for(int i = 0; i < cols; i++)
        {
            JEPLUpdateColumnPropertyInfo columnPropInfo = new JEPLUpdateColumnPropertyInfo();
            columnPropInfo.columnDesc.setName( columnNameList.get(i) );
            columnPropInfo.columnDesc.setAutoIncrement( autoIncColumnList.get(i) );              
            columnArray[i] = columnPropInfo;
        }

        result = dbMetaData.getPrimaryKeys(catalog, schemaPattern, tableName);         
        
        while(result.next())
        {
            String keyColumnName = result.getString("COLUMN_NAME");
            for(JEPLUpdateColumnPropertyInfo prop : columnArray)
            {
                if (keyColumnName.equals(prop.columnDesc.getName())) 
                {
                    prop.columnDesc.setPrimaryKey( true ); // Si no pasa por aquí será false
                    break;
                }
            }
        }
        result.close();
           
        for (JEPLUpdateColumnPropertyInfo columnPropInfo : columnArray)
        {
            String columnName = columnPropInfo.columnDesc.getName();
            String columnNameLow = columnName.toLowerCase();
            JEPLBeanPropertyDescriptorImpl beanProp = propertyMap.get(columnNameLow);
            if (beanProp != null)
            {
                Method getter = beanProp.getReadMethod();  
                columnPropInfo.getter = getter;
            }
        }
    }
}
