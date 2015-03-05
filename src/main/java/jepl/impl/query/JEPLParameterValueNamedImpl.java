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

/**
 *
 * @author jmarranz
 */
public class JEPLParameterValueNamedImpl<T> extends JEPLParameterValueImpl<T> //implements JEPLParameter<T>
{
    protected String name;
    protected JEPLParameterWithNameImpl<T> param;

    public JEPLParameterValueNamedImpl(String name)
    {
        this.name = name;
    }

    public JEPLParameterWithNameImpl<T> getJEPLParameterWithName()
    {
        return param;
    }

    public void setJEPLParameterWithName(JEPLParameterWithNameImpl<T> param)
    {
        this.param = param;
    }

    public String getName()
    {
        return name;
    }

    public Integer getPosition()
    {
        return null;
    }
}
