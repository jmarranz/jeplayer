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
    protected Map<String,JEPLPropertyDescriptorImpl> propertyMap; // Será solo lectura desde su creación
    protected JEPLUpdateDAOBeanMapper<T> beanMapper;
    protected String tableName;
    
    public JEPLUpdateDAOListenerDefaultImpl(Class<T> clasz,JEPLUpdateDAOBeanMapper<T> beanMapper) 
    {
        this.clasz = clasz;
        this.beanMapper = beanMapper;  
        this.tableName = clasz.getClass().getSimpleName().toLowerCase();
        this.propertyMap = JEPLPropertyDescriptorImpl.introspect(clasz);
    }
    
    @Override
    public String getTable(JEPLConnection jcon,T obj) 
    {
        return tableName;
    }

    @Override
    public Map.Entry<String,Object>[] getColumnNameValues(JEPLConnection jcon,T obj, JEPLPersistAction action) throws Exception
    {
        JEPLUpdateBeanInfo beanInfo = ((JEPLConnectionImpl)jcon).getJEPLUpdateBeanInfo(getTable(jcon,obj),propertyMap);
        int cols = beanInfo.columnNameArr.size();
        Map.Entry<String,Object>[] result = new SimpleEntry[cols]; 
        for (int col = 0; col < cols; col++)
        {
            String columnName = beanInfo.columnNameArr.get(col);
            Method getter = beanInfo.getterArr[col];

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
                value = beanMapper.getColumnInBean(obj,jcon,columnName,getter,action);
            }
            else
            {
                value = getColumnInBean(obj,columnName,getter);
                value = JEPLUtilImpl.cast(value, returnClass);                 
            }
            
            result[col] = new SimpleEntry<String,Object>(columnName,value);
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
