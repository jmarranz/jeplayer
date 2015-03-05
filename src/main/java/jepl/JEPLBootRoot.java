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

import jepl.impl.JEPLBootRootImpl;

/**
 * This class is the root factory of JEPLayer.
 *
 * You can get the singleton object of this class calling {@link #get()}.
 * 
 * @author jmarranz
 */
public abstract class JEPLBootRoot implements JEPLUserData
{
    protected static JEPLBootRoot root = new JEPLBootRootImpl();
    
    /**
     * Constructs a new object
     */
    public JEPLBootRoot()
    {
    }

    /**
     * Returns the singleton object of this class.
     * 
     * @return the singleton root object of the framework.
     */
    public static JEPLBootRoot get()
    {
        return root;
    }

    /**
     * Returns the version of the library
     *
     * @return the library version.
     */
    public abstract String getVersion();

    /**
     * Creates a factory object to create Java Transaction API based data sources
     * managed by JEPLLayer.
     * 
     * @return a new factory object for wrapping JTA data sources.
     */
    public abstract JEPLBootJTA createJEPLBootJTA();

    /**
     * Creates a factory object to create JDBC based data sources
     * managed by JEPLLayer.
     * 
     * @return a new factory object for wrapping JDBC data sources.
     */
    public abstract JEPLBootNonJTA createJEPLBootNonJTA();
}
