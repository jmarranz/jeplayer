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

import javax.sql.DataSource;

/**
 * This interface represents a factory of JDBC based data sources (non-JTA)
 * managed by JEPLLayer.
 *
 * @author jmarranz
 * @see JEPLBootRoot#createJEPLBootNonJTA()
 */
public interface JEPLBootNonJTA extends JEPLBoot
{
    /**
     * Creates a new {@link JEPLDataSource} wrapping the specified DataSource
     *
     * @param ds the DataSource to wrap.
     * @return a new {@link JEPLDataSource}
     */
    public JEPLNonJTADataSource createJEPLNonJTADataSource(DataSource ds);
}
