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
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import jepl.*;
import static jepl.JEPLUpdateDAOBeanMapper.NO_VALUE;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLUtilImpl;

/**
 *
 * @author jmarranz
 * @param <T>
 */
public class JEPLUpdateDAOListenerDefaultImpl<T> implements JEPLUpdateDAOListenerDefault<T>
{
    protected Class<T> clasz;
    protected Map<String,JEPLBeanPropertyDescriptorImpl> propertyMap; // Será solo lectura desde su creación
    protected JEPLUpdateDAOBeanMapper<T> beanMapper;
    protected String tableNameLowerCase;
    
    public JEPLUpdateDAOListenerDefaultImpl(Class<T> clasz,JEPLUpdateDAOBeanMapper<T> beanMapper) 
    {
        this.clasz = clasz;
        this.beanMapper = beanMapper;  
        this.tableNameLowerCase = clasz.getSimpleName().toLowerCase();
        this.propertyMap = JEPLBeanPropertyDescriptorRegistryImpl.introspect(clasz);
    }
    
    @Override
    public String getTable(JEPLConnection jcon,T obj) 
    {
        return tableNameLowerCase;
    }

    @Override
    public Map.Entry<JEPLColumnDesc,Object>[] getColumnDescAndValues(JEPLConnection jcon,T obj, JEPLPersistAction action) throws Exception
    {
        JEPLUpdateColumnPropertyInfoList beanInfo = ((JEPLConnectionImpl)jcon).getJEPLUpdateColumnPropertyInfoList(tableNameLowerCase,propertyMap);
        JEPLUpdateColumnPropertyInfo[] columnArray = beanInfo.columnArray;
        int cols = columnArray.length;
        Map.Entry<JEPLColumnDesc,Object>[] result = new SimpleEntry[cols]; 
        for (int col = 0; col < cols; col++)
        {
            JEPLUpdateColumnPropertyInfo columnPropInfo = columnArray[col];
            JEPLColumnDesc columnDesc = columnPropInfo.columnDesc;
            String columnNameLowerCase = columnDesc.getName();
            Method getter = columnPropInfo.getter;

            Class<?> returnClass;
            if (getter != null)
            {
                returnClass = getter.getReturnType();
            }
            else
            {
                returnClass = Object.class;
            }

            Object value;
            if (beanMapper != null)
            {               
                value = beanMapper.getColumnFromBean(obj,jcon,columnNameLowerCase,getter,action);
                if (value == NO_VALUE)
                {
                    value = getColumnInBean(obj,columnNameLowerCase,getter);
                    value = JEPLUtilImpl.cast(value, returnClass);                    
                } 
            }
            else
            {
                value = getColumnInBean(obj,columnNameLowerCase,getter);
                value = JEPLUtilImpl.cast(value, returnClass);                 
            }
            
            result[col] = new SimpleEntry<JEPLColumnDesc,Object>(columnDesc,value);
        }             
        
        return result;
    }    
    
    @Override
    public Class<T> getBeanClass()
    {
        return clasz;
    }
  
    
    @Override
    public JEPLUpdateDAOBeanMapper<T> getJEPLUpdateDAOBeanMapper()
    {
        return beanMapper;
    }

    public Object getColumnInBean(T obj,String columnName,Method getter) throws Exception
    {
        if (getter == null) throw new JEPLException("There is no getter method for " + columnName); // El bean-mapper tuvo su oportunidad pero no está definido

        return getter.invoke(obj,(Object[])null);
    }
}
