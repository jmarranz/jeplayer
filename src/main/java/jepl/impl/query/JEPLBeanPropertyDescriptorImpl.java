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

/**
 *
 * @author jmarranz
 */
public class JEPLBeanPropertyDescriptorImpl 
{
    protected String propName; 
    protected Method methodSet;
    protected Method methodGet;    
    protected Class<?> propertyClass;
    
    protected JEPLBeanPropertyDescriptorImpl(String propName)
    {
        this.propName = propName;
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
    

}
