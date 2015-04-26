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
 * {@link TO DO}
 * 
 * @author jmarranz
 */
public class JEPLColumnDesc 
{
    protected String name;
    protected boolean autoIncrement;    
    protected boolean primarykey;

    public JEPLColumnDesc() 
    {
    }    
    
    public JEPLColumnDesc(String name, boolean autoIncrement, boolean primarykey) 
    {
        this.name = name;
        this.autoIncrement = autoIncrement;
        this.primarykey = primarykey;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isPrimaryKey() {
        return primarykey;
    }

    public void setPrimaryKey(boolean primarykey) {
        this.primarykey = primarykey;
    }
    
}
