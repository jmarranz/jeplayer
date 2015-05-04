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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDataSourceImpl;
import jepl.impl.nonjta.android.JEPLNonJTAConnectionSQLDroidImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLUpdateColumnPropertyInfoList
{
    public JEPLUpdateColumnPropertyInfo[] columnArray; 

    public JEPLUpdateColumnPropertyInfoList(JEPLConnectionImpl jcon,String tableNameLowerCase,Map<String,JEPLBeanPropertyDescriptorImpl> propertyMap) throws SQLException
    {
        // http://tutorials.jenkov.com/jdbc/databasemetadata.html#listing-columns-in-a-table
        // http://www.herongyang.com/JDBC/sqljdbc-jar-Column-List.html
        
        JEPLDataSourceImpl jds = jcon.getJEPLDataSourceImpl();
        Connection con = jcon.getConnection();
        DatabaseMetaData dbMetaData =  con.getMetaData();
        
        boolean sqlDroid = (jcon instanceof JEPLNonJTAConnectionSQLDroidImpl); // Android, SQLite y SQLDroid como driver 
        
        String   catalog           = null;
        String   schemaPattern     = null;
        String   columnNamePattern = null;

        ResultSet result = dbMetaData.getColumns(catalog, schemaPattern,  tableNameLowerCase, columnNamePattern);

        ArrayList<String> columnNameList = new ArrayList<String>();
        ArrayList<Boolean> autoIncColumnList = new ArrayList<Boolean>();    
        ArrayList<Boolean> generatedColumnList = new ArrayList<Boolean>();        
        
        while(result.next())
        {
            String columnNameLowerCase = result.getString("COLUMN_NAME").toLowerCase();
            columnNameList.add(columnNameLowerCase);

            Boolean autoIncrement = sqlDroid ? autoIncrement = Boolean.FALSE /* Por ahora */ : "YES".equals(result.getString("IS_AUTOINCREMENT"));        
            autoIncColumnList.add(autoIncrement);
            
            Boolean generatedColumn;
            if (jds.generatedColumnMetadataIsSupported)
            {
                try
                {
                    // IS_GENERATEDCOLUMN se define en Java 1.7
                    // Pero en Java 7 (1.7) aunque sea con compatibilidad 1.6 si el driver lo soporta IS_GENERATEDCOLUMN funciona
                    generatedColumn = "YES".equals(result.getString("IS_GENERATEDCOLUMN"));   
                }
                catch(Exception ex)
                {
                    generatedColumn = Boolean.FALSE; // El driver no lo soporta
                    jds.generatedColumnMetadataIsSupported = false;
                }
            }
            else
                generatedColumn = Boolean.FALSE;
            
            generatedColumnList.add(generatedColumn);
        }              
        result.close();
        
        int cols = columnNameList.size();
        
        this.columnArray = new JEPLUpdateColumnPropertyInfo[cols];
        for(int i = 0; i < cols; i++)
        {
            JEPLUpdateColumnPropertyInfo columnPropInfo = new JEPLUpdateColumnPropertyInfo();
            columnPropInfo.columnDesc.setName( columnNameList.get(i) );
            columnPropInfo.columnDesc.setAutoIncrement( autoIncColumnList.get(i) );              
            columnPropInfo.columnDesc.setGenerated( generatedColumnList.get(i) );
            
            columnArray[i] = columnPropInfo;
        }

        result = dbMetaData.getPrimaryKeys(catalog, schemaPattern, tableNameLowerCase);
        while(result.next())
        {
            String keyColumnNameLowerCase = result.getString("COLUMN_NAME").toLowerCase();
            for(JEPLUpdateColumnPropertyInfo prop : columnArray)
            {
                if (keyColumnNameLowerCase.equals(prop.columnDesc.getName()))
                {           
                    if (sqlDroid) // Lo normal es que sólo haya una clave primaria, como haya más la hemos liado            
                    {
                        Boolean autoIncrement = getAutoIncrementInSQLDroid(con,tableNameLowerCase);                    
                        prop.columnDesc.setAutoIncrement(autoIncrement);
                    }
                    
                    prop.columnDesc.setPrimaryKey(true); // Si no pasa por aquí será false
                    break;
                }
            }
        }
        result.close();
           
        if (sqlDroid)
        {
            getForeignKeyListInSQLDroid(con,tableNameLowerCase,columnArray);
        }
        else
        {
            result = dbMetaData.getImportedKeys(catalog, schemaPattern, tableNameLowerCase);    // Foreign keys
            while (result.next())
            {
                String keyColumnNameLowerCase = result.getString("FKCOLUMN_NAME").toLowerCase();
                for (JEPLUpdateColumnPropertyInfo prop : columnArray)
                {
                    if (keyColumnNameLowerCase.equals(prop.columnDesc.getName()))
                    {
                        prop.columnDesc.setImportedKey(true); // Si no pasa por aquí será false
                        break;
                    }
                }
            }
            result.close();
        }
        
        for (JEPLUpdateColumnPropertyInfo columnPropInfo : columnArray)
        {
            String columnNameLowerCase = columnPropInfo.columnDesc.getName();
            JEPLBeanPropertyDescriptorImpl beanProp = propertyMap.get(columnNameLowerCase);
            if (beanProp != null)
            {
                Method getter = beanProp.getReadMethod();
                columnPropInfo.getter = getter;
            }
        }
    }

    private static boolean getAutoIncrementInSQLDroid(Connection con,String tableNameLowerCase) throws SQLException
    {
        // http://stackoverflow.com/questions/18694393/how-could-you-get-if-a-table-is-autoincrement-or-not-from-the-metadata-of-an-sql
        // Recuerda que el executeUpdate() en SQLDroid funciona mal, usamos un ResultSet
        PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND LOWER(name) = '" + tableNameLowerCase + "' AND sql LIKE '%AUTOINCREMENT%' "  );
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean result = rs.getInt(1) == 1;
        rs.close();
        stmt.close();
        return result;
    }
    
    private static void getForeignKeyListInSQLDroid(Connection con,String tableNameLowerCase,JEPLUpdateColumnPropertyInfo[] columnArray) throws SQLException
    {
        // Por desgracia la v1.0.3 no tiene definida getImportedKeys, está en la versión de desarrollo
        // https://github.com/SQLDroid/SQLDroid/blob/master/src/main/java/org/sqldroid/SQLDroidDatabaseMetaData.java
        // https://www.sqlite.org/pragma.html#pragma_foreign_key_list
        PreparedStatement stmt = con.prepareStatement("PRAGMA foreign_key_list(" + tableNameLowerCase + ")" );
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            String keyColumnNameLowerCase = rs.getString( 4 ).toLowerCase();            
            
            for (JEPLUpdateColumnPropertyInfo prop : columnArray)
            {
                if (keyColumnNameLowerCase.equals(prop.columnDesc.getName()))
                {
                    prop.columnDesc.setImportedKey(true); // Si no pasa por aquí será false
                    break;
                }
            }            
        }
        rs.close();
        stmt.close();
    }    
    
    
    
}
