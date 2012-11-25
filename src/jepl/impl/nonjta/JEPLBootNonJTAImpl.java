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

package jepl.impl.nonjta;

import javax.sql.DataSource;
import jepl.JEPLBootNonJTA;
import jepl.JEPLNonJTADataSource;
import jepl.impl.JEPLBootImpl;
import jepl.impl.JEPLBootRootImpl;
import jepl.impl.nonjta.android.JEPLNonJTADataSourceAndroidImpl;

/**
 * 
 * @author jmarranz
 */
public class JEPLBootNonJTAImpl extends JEPLBootImpl implements JEPLBootNonJTA
{
    public static final boolean android = System.getProperty("java.specification.name").contains("Dalvik");

    public JEPLBootNonJTAImpl(JEPLBootRootImpl root)
    {
        super(root);
    }

    public JEPLNonJTADataSource createJEPLNonJTADataSource(DataSource ds)
    {
    	if (android)
            return new JEPLNonJTADataSourceAndroidImpl(this,ds);
    	else
    		return new JEPLNonJTADataSourceDefaultImpl(this,ds);
    }
}
