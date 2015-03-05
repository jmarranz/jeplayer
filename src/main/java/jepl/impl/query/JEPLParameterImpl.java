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

import jepl.JEPLException;
import jepl.JEPLParameter;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLParameterImpl<T> implements JEPLParameter<T>
{
    protected JEPLParameterDecImpl dec;
    protected JEPLParameterValueImpl<T> paramValue;
    
    public JEPLParameterImpl(JEPLParameterDecImpl dec)
    {
        this.dec = dec;
    }

    public int getJDBCParamPosition()
    {
        return dec.getJDBCParamPosition();
    }

    public String getName()
    {
        return dec.getName();
    }

    public Integer getPosition()
    {
        return dec.getPosition();
    }

    public T getValue()
    {
        if (!paramValue.isDefined()) throw new JEPLException(getErrorMessageValueNotDefined());
        return paramValue.getValue();
    }

    public boolean isBound()
    {
        return paramValue.isDefined();
    }

    public abstract String getErrorMessageValueNotDefined();
}
