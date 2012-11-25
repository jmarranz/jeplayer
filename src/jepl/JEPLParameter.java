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
package jepl;

/**
 * Represents a parameter declaration in a SQL statement.
 *
 * @author jmarranz
 * @see JEPLDALQuery#getJEPLParameter(int)
 * @see JEPLDALQuery#getJEPLParameter(String)
 */
public interface JEPLParameter<T>
{
    /**
     * Return the parameter name, or null if the parameter is not a named parameter.
     * @return the parameter name.
     */
    public String getName();

    /**
     * Return the parameter position, or null if the parameter is not a positional parameter.
     *
     * @return position of parameter.
     */
    public Integer getPosition();
}
