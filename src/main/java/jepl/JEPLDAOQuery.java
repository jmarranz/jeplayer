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
 * This interface is implemented by JEPLayer to provide a utility object using a fluid API to execute the
 * specified query when creating an instance of this type returning user defined data model objects.
 *
 * <p>Is based on (inherits from) {@link JEPLDALQuery} adding new methods to return
 * user defined data model objects of the class specified when creating the parent
 * {@link JEPLDAO} used to create an instance of this type.</p>
 *
 * <p>Many methods of {@link JEPLDALQuery} are repeated here just to provide the fluid API in this
 * level, behavior is the same.</p>
 *
 * @see JEPLDAO#createJEPLDAOQuery(String)
 * @author jmarranz
 */
public interface JEPLDAOQuery<T> extends JEPLDALQuery
{
    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#addJEPLListener(JEPLListener)
     */
    public JEPLDAOQuery<T> addJEPLListener(JEPLListener listener);
    
    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setParameter(int,Object)
     */
    public JEPLDAOQuery<T> setParameter(int position,Object value);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setParameter(String,Object)
     */
    public JEPLDAOQuery<T> setParameter(String name,Object value);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#addParameter(Object)
     */
    public JEPLDAOQuery<T> addParameter(Object value);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#addParameters(Object...)
     */
    public JEPLDAOQuery<T> addParameters(Object... values);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setStrictMinRows(int)
     */
    public JEPLDAOQuery<T> setStrictMinRows(int value);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setStrictMaxRows(int)
     */
    public JEPLDAOQuery<T> setStrictMaxRows(int value);

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setFirstResult(int)
     */
    public JEPLDAOQuery<T> setFirstResult(int startPosition);    

    /**
     * This method is the same as the method in the parent interface just to provide the fluid API
     * in this level.
     *
     * @see JEPLDALQuery#setMaxResults(int)
     */
    public JEPLDAOQuery<T> setMaxResults(int maxResult);

    /**
     * This method must be used to execute a SELECT statement,
     * it returns a list of user defined data model objects.
     *
     * @return the user defined data model object or null.
     */
    public List<T> getResultList();

    /**
     * This method must be used to execute a SELECT statement returning a single or none
     * user defined data model object.
     *
     * <p>If more than one result is returned a {@link JEPLException} is thrown.</p>
     *
     * @return the user defined data model object or null.
     */
    public T getSingleResult();

    /**
     * This method must be used to execute a SELECT statement,
     * it returns a connected result set ready to iterate and get user defined data model objects
     * on demand from database.
     *
     * @return the connected result set of data model objects.
     */
    public JEPLResultSetDAO<T> getJEPLResultSetDAO();
}
