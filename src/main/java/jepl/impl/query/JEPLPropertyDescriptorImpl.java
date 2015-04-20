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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author jmarranz
 */
public class JEPLPropertyDescriptorImpl 
{
    protected String propName; 
    protected Method methodSet;
    protected Method methodGet;    
    protected Class<?> propertyClass;
    
    protected JEPLPropertyDescriptorImpl(String propName)
    {
        this.propName = propName;
    }

    public static Map<String,JEPLPropertyDescriptorImpl> introspect(Class<?> clasz)
    {
        // Podríamos usar:
        // BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
        // PropertyDescriptor[] beanProps = beanInfo.getPropertyDescriptors();
        // pero no existe en Android, por lo que tenemos que re-hacerlo a mano
        
        // Obtenemos los métodos con Type getName()/setName(Type)        
        Map<String,JEPLPropertyDescriptorImpl> properties = new HashMap<String,JEPLPropertyDescriptorImpl>();        
        Method[] publicMethods = getPublicMethods(clasz);
        for (Method method : publicMethods)
        {
            if (method == null) continue;
            String methodName = method.getName();
            if (methodName.startsWith("get"))
            {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 0) continue;
                Class<?> returnClass = method.getReturnType();
                if (returnClass.equals(void.class)) continue;
                
                String propName = getPropertyName(method);
                if (propName == null) continue;                
                String propNameLowcase = propName.toLowerCase(); // Nos interesa indexar en minúsculas porque luego al matchear con columnas de BD debemos admitir combinaciones de mayúsculas y minúsculas
                
                JEPLPropertyDescriptorImpl property = new JEPLPropertyDescriptorImpl(propNameLowcase);
                properties.put(propNameLowcase, property); // Si 

                property.setReadMethod(method,returnClass);
            }
        }

        for (Method method : publicMethods)
        {
            if (method == null) continue;
            String methodName = method.getName();
            if (methodName.startsWith("set"))
            {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) continue;                
                Class<?> returnClass = method.getReturnType();
                if (!returnClass.equals(void.class)) continue;                               
                
                String propName = getPropertyName(method);
                if (propName == null) continue;                
                String propNameLowcase = propName.toLowerCase(); 
                
                JEPLPropertyDescriptorImpl property = properties.get(propNameLowcase);
                if (property == null) continue; // set que no tiene get, no lo consideramos              
                
                Class<?> paramClass = method.getParameterTypes()[0];
                if (!property.getPropertyClass().equals(paramClass)) continue; // El tipo del parámetro del set no se corresponde con el retorno del get
                
                property.setWriteMethod(method);
            }
        }        
        
        // Eliminamos los que no tienen set
        for(Iterator<Map.Entry<String,JEPLPropertyDescriptorImpl>> it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<String,JEPLPropertyDescriptorImpl> entry = it.next();
            JEPLPropertyDescriptorImpl property = entry.getValue();
            if (property.getWriteMethod() == null)
            {
                it.remove();
            }
        }
        
        return properties;
    }
        
    public String getName()
    {
        return propName;
    }
    
    public Method getReadMethod()    
    {
        return methodGet;
    }
    
    public void setReadMethod(Method methodGet,Class<?> propertyClass)    
    {
        this.methodGet = methodGet;
        this.propertyClass = propertyClass;
    }    
    
    public Class<?> getPropertyClass()
    {
        return propertyClass;
    }
    
    public Method getWriteMethod()    
    {
        return methodSet;
    }
    
    public void setWriteMethod(Method methodSet)    
    {
        this.methodSet = methodSet;
    }    
    
    private static String getPropertyName(Method method)
    {
        String propName = method.getName();
        propName = propName.substring(3);
        if (propName.isEmpty()) return null; // Muy raro, caso de llamarse "get"
        char first = propName.charAt(0);
        first = Character.toLowerCase(first);                
        propName = first + propName.substring(1);    
        return propName;
    }
    

    private static Method[] getPublicMethods(Class<?> clz) 
    {
        Method[] result = clz.getMethods();

        // Null los no públicos.
        for (int i = 0; i < result.length; i++) {
            Method method = result[i];
            int mods = method.getModifiers();
            if (!Modifier.isPublic(mods)) {
                result[i] = null;
            }
        }    

	return result;
    }
    
}
