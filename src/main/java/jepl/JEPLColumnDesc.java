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
    protected boolean primaryKey;
    protected boolean importedKey;
    
    /**
     * {@link TO DO}
     */
    public JEPLColumnDesc() 
    {
    }      
    
    /**
     *  {@link TO DO}
     * @param name 
     */
    public JEPLColumnDesc(String name) 
    {
        this.name = name;
        this.autoIncrement = false;
        this.primaryKey = false;
        this.importedKey = false;        
    }        
    

    /**
     * {@link TO DO}
     * @return 
     */    
    public String getName() {
        return name;
    }

    /**
     * {@link TO DO}
     * @param name 
     * @return  
     */
    public JEPLColumnDesc setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * {@link TO DO}
     * @return 
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    /**
     * {@link TO DO}
     * @param autoIncrement 
     * @return  
     */
    public JEPLColumnDesc setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    /**
     * {@link TO DO}
     * @return 
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * {@link TO DO}
     * @param primaryKey 
     * @return  
     */
    public JEPLColumnDesc setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    /**
     * {@link TO DO}
     * @return 
     */
    public boolean isImportedKey() {
        return importedKey;
    }

    /**
     * {@link TO DO}
     * @param importedKey 
     * @return  
     */
    public JEPLColumnDesc setImportedKey(boolean importedKey) {
        this.importedKey = importedKey;
        return this;
    }    
}
