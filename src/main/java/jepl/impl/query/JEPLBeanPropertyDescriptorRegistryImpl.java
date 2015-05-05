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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author jmarranz
 */
public class JEPLBeanPropertyDescriptorRegistryImpl {
    
    private static final Map<Class,Map<String,JEPLBeanPropertyDescriptorImpl>> propertiesMapCached = 
            Collections.synchronizedMap(new HashMap<Class,Map<String,JEPLBeanPropertyDescriptorImpl>>());
    
    public static Map<String,JEPLBeanPropertyDescriptorImpl> introspect(Class<?> clasz)
    {  
        Map<String,JEPLBeanPropertyDescriptorImpl> properties = propertiesMapCached.get(clasz);
        if (properties != null)
            return properties;
            
        // No pasa nada si dos hilos están a la vez con la misma clase, ganará el último, en ambos hilos el objeto generado tiene el mismo contenido
        properties = introspectInternal(clasz);
        
        propertiesMapCached.put(clasz, properties);
        
        return properties;
    }
    
    private static Map<String,JEPLBeanPropertyDescriptorImpl> introspectInternal(Class<?> clasz)
    {
        // Podríamos usar:
        // BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
        // PropertyDescriptor[] beanProps = beanInfo.getPropertyDescriptors();
        // pero no existe en Android, por lo que tenemos que re-hacerlo a mano
        
        // Obtenemos los métodos con Type getName()/setName(Type)        
        Map<String,JEPLBeanPropertyDescriptorImpl> properties = new HashMap<String,JEPLBeanPropertyDescriptorImpl>();        
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
                
                String propNameLowcase = getPropertyNameLowerCase(method);
                if (propNameLowcase == null) continue;                 
                
                JEPLBeanPropertyDescriptorImpl property = new JEPLBeanPropertyDescriptorImpl(propNameLowcase);
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
                
                String propNameLowcase = getPropertyNameLowerCase(method);
                if (propNameLowcase == null) continue;                 
                
                JEPLBeanPropertyDescriptorImpl property = properties.get(propNameLowcase);
                if (property == null) continue; // set que no tiene get, no lo consideramos              
                
                Class<?> paramClass = method.getParameterTypes()[0];
                if (!property.getPropertyClass().equals(paramClass)) continue; // El tipo del parámetro del set no se corresponde con el retorno del get
                
                property.setWriteMethod(method);
            }
        }        
        
        // Eliminamos los que no tienen set
        for(Iterator<Map.Entry<String,JEPLBeanPropertyDescriptorImpl>> it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<String,JEPLBeanPropertyDescriptorImpl> entry = it.next();
            JEPLBeanPropertyDescriptorImpl property = entry.getValue();
            if (property.getWriteMethod() == null)
            {
                it.remove();
            }
        }
        
        return properties;
    }
    
    private static String getPropertyNameLowerCase(Method method)
    {
        String propName = method.getName();
        propName = propName.substring(3);
        if (propName.isEmpty()) return null; // Muy raro, caso de llamarse "get"
        char first = propName.charAt(0);
        first = Character.toLowerCase(first);                
        propName = first + propName.substring(1);    
        return propName.toLowerCase(); // Nos interesa indexar en minúsculas porque luego al matchear con columnas de BD debemos admitir combinaciones de mayúsculas y minúsculas                
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
