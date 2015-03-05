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

/*
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import jepl.JEPLException;
*/

/**
 *
 * @author jmarranz
 */
public class JEPLPropertyDescriptorJavaBeansImpl 
{
    /* Por si rescatamos la implementación que quitamos por Android
    protected PropertyDescriptor desc;
    
    protected JEPLPropertyDescriptorJavaBeansImpl(PropertyDescriptor desc)
    {
        this.desc = desc;
    }

    public static JEPLPropertyDescriptorJavaBeansImpl[] introspect(Class clasz)
    {
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
            PropertyDescriptor[] beanProps = beanInfo.getPropertyDescriptors();
            JEPLPropertyDescriptorJavaBeansImpl[] props = new JEPLPropertyDescriptorJavaBeansImpl[beanProps.length];        
            for(int i = 0; i < beanProps.length; i++)
                props[i] = new JEPLPropertyDescriptorJavaBeansImpl(beanProps[i]);
            return props;
        }
        catch(IntrospectionException ex)
        {
            throw new JEPLException(ex);
        }
    }
    
    
    public String getName()
    {
        return desc.getName();
    }
    
    public Method getWriteMethod()    
    {
        return desc.getWriteMethod();
    }
    */
}
