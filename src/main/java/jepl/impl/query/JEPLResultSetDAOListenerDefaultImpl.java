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
import java.util.Map;
import jepl.*;
import jepl.impl.JEPLUtilImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLResultSetDAOListenerDefaultImpl<T> implements JEPLResultSetDAOListenerDefault<T>
{
    protected Class<T> clasz;
    protected Map<String,JEPLBeanPropertyDescriptorImpl> propertyMap; // Será solo lectura desde su creación
    protected JEPLResultSetDAOBeanMapper<T> rowBeanMapper;

    public JEPLResultSetDAOListenerDefaultImpl(Class<T> clasz,JEPLResultSetDAOBeanMapper<T> rowBeanMapper) 
    {
        this.clasz = clasz;
        this.rowBeanMapper = rowBeanMapper;
        
        this.propertyMap = JEPLBeanPropertyDescriptorImpl.introspect(clasz);
    }

    
    @Override
    public Class<T> getBeanClass()
    {
        return clasz;
    }
    
    @Override
    public JEPLResultSetDAOBeanMapper<T> getJEPLResultSetDAOBeanMapper()
    {
        return rowBeanMapper;
    }

    @Override
    public void setupJEPLResultSet(JEPLResultSet jrs,JEPLTask<?> task) throws Exception
    {
        // La primera llamada inicializa el beanInfo
        ((JEPLResultSetImpl)jrs).getJEPLResultSetColumnPropertyInfoList(propertyMap);
    }

    @Override
    public T createObject(JEPLResultSet jrs) throws Exception
    {
        return clasz.newInstance();
    }

    @Override
    public void fillObject(T obj,JEPLResultSet jrs) throws Exception
    {
        ResultSet rs = jrs.getResultSet();
        JEPLDAL dal = jrs.getJEPLStatement().getJEPLDAL();
        JEPLResultSetColumnPropertyInfoList beanInfoList = ((JEPLResultSetImpl)jrs).getJEPLResultSetColumnPropertyInfoList(propertyMap);

        int cols = beanInfoList.columnArray.length;
        for (int col = 1; col <= cols; col++)
        {
            JEPLResultSetColumnPropertyInfo columnDesc = beanInfoList.columnArray[col - 1];
            String columnName = columnDesc.columnName;
            Method setter = columnDesc.setter;

            Class<?> paramClass;
            if (setter != null)
            {
                Class<?>[] params = setter.getParameterTypes();
                paramClass = params[0];
            }
            else
            {
                paramClass = Object.class;
            }

            Object value = JEPLUtilImpl.getResultSetColumnObject(rs, col, paramClass);
            value = dal.cast(value, paramClass);
            if (rowBeanMapper != null)
            {               
                boolean isSet = rowBeanMapper.setColumnInBean(obj, jrs, col, columnName, value, setter);
                if (!isSet) setColumnInBean(obj,value, setter);
            }
            else
            {
                setColumnInBean(obj, value , setter);
            }
        }      
    }

    public void setColumnInBean(T obj,Object value,Method setter) throws Exception
    {
        if (setter == null) return; // No podemos

        setter.invoke(obj, new Object[] { value });
    }
}
