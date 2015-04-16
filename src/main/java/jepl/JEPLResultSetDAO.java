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

import java.util.List;

/**
 * This interface is a wrapper of a standard JDBC ResultSet to load user defined data model objects.
 *
 * @param <T> the type of elements of this result set
 * @see JEPLResultSetDAOListener
 * @author jmarranz
 */
public interface JEPLResultSetDAO<T> extends JEPLResultSet,List<T>
{
    /**
     * Returns the current row as a user defined data model object.
     *
     * @return the user defined data model object created with data of the current row.
     */
    public T getObject();
}
