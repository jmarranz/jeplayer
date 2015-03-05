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
import java.util.Map;

/**
 *
 * @author jmarranz
 */
public class JEPLPropertyDescriptorImpl 
{
    protected String propName; 
    protected Method methodSet;
    
    protected JEPLPropertyDescriptorImpl(String propName,Method methodSet)
    {
        this.propName = propName;
        this.methodSet = methodSet;
    }

    public static Map<String,JEPLPropertyDescriptorImpl> introspect(Class<?> clasz)
    {
        // Obtenemos los métodos con getName()/setName(Type) 
        Map<String,Method> mapGetMethods = new HashMap<String,Method>();
        Map<String,Method> mapSetMethods = new HashMap<String,Method>();        
        Method[] publicMethods = getPublicMethods(clasz);
        for(int i = 0; i < publicMethods.length; i++)
        {
            Method method = publicMethods[i];
            if (method == null) continue;
            if (method.getName().startsWith("get"))
            {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 0) continue;
                Class<?> returnClass = method.getReturnType();
                if (returnClass.equals(void.class)) continue;
                
                String propName = getPropertyName(method);
                if (propName == null) continue;
                mapGetMethods.put(propName,method);
            }
            else if (method.getName().startsWith("set"))
            {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) continue;
                String propName = getPropertyName(method);
                if (propName == null) continue;                
                mapSetMethods.put(propName,method);                
            }
        }
        
        Map<String,JEPLPropertyDescriptorImpl> propertyMap = new HashMap<String,JEPLPropertyDescriptorImpl>();        
        
        for(Map.Entry<String,Method> methodGet : mapGetMethods.entrySet())
        {
            String propName = methodGet.getKey();
            Method methodSet = mapSetMethods.get(propName);
            if (methodSet == null) continue;
            Class<?> returnClass = methodGet.getValue().getReturnType();
            Class<?> paramClass = methodSet.getParameterTypes()[0];
            if (!returnClass.equals(paramClass)) continue;
            String propNameLowcase = propName.toLowerCase(); // Nos interesa indexar en minúsculas porque luego al matchear con columnas de BD debemos admitir combinaciones de mayúsculas y minúsculas
            propertyMap.put(propNameLowcase,new JEPLPropertyDescriptorImpl(propName,methodSet));
        }
        
        return propertyMap;
    }
        
    public String getName()
    {
        return propName;
    }
    
    public Method getWriteMethod()    
    {
        return methodSet;
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
