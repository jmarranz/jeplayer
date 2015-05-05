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
 * This interface is implemented by JEPLayer to provide a utility object using a fluid API to execute the
 * specified query when creating an instance of this type.
 *
 * <p>Is inspired on JPA 2 Query interface but you are not going to find the same exact API and behavior.</p>
 *
 * @see JEPLDAL#createJEPLDALQuery(String)
 * @author jmarranz
 */
public interface JEPLDALQuery
{
    /**
     * Returns the parameter descriptor in the specified position declared using ? or ?N
     * in the SQL sentence of this query.
     *
     * @param position the parameter position. Starting in 1.
     * @return the parameter descriptor.
     * @see #setParameter(int,Object)
     */
    public JEPLParameter<?> getJEPLParameter(int position);

    /**
     * Returns the parameter descriptor in the specified position declared using ? or ?N
     * in the SQL sentence of this query.
     *
     * @param <T> the type of the expected parameter value.
     * @param position  the parameter position. Starting in 1.
     * @param type the type of the expected parameter value.
     * @return the parameter descriptor.
     * @see #getJEPLParameter(int)
     */
    public <T> JEPLParameter<T> getJEPLParameter(int position,Class<T> type);

    /**
     * Sets the parameter value in the required position to replace ? or ?N expressions in SQL sentence.
     *
     * @param position the parameter position. Starting in 1.
     * @param value the parameter value.
     * @return the same instance (fluid API).
     */
    public JEPLDALQuery setParameter(int position,Object value);

    /**
     * Returns the parameter descriptor with the specified name declared using :name sintax
     * in the SQL sentence of this query.
     *
     * @param name the parameter name.
     * @return the parameter descriptor.
     * @see #setParameter(String,Object)
     */
    public JEPLParameter<?> getJEPLParameter(String name);

    /**
     * Returns the parameter descriptor with the specified name declared using :name sintax
     * in the SQL sentence of this query.
     *
     * @param <T> the type of the expected parameter value.
     * @param name the parameter name.
     * @param type the type of the expected parameter value.
     * @return the parameter descriptor.
     * @see #getJEPLParameter(String)
     */
    public <T> JEPLParameter<T> getJEPLParameter(String name,Class<T> type);

    /**
     * Sets the parameter value for the specified name declared using :name sintax
     * in the SQL sentence of this query.
     *
     * @param name the parameter name.
     * @param value the parameter value.
     * @return the same instance (fluid API).
     */
    public JEPLDALQuery setParameter(String name,Object value);

    /**
     * Adds the specified parameter value following the parameter sequence specified with ?, ?N or :name
     * in the SQL sentence of this query.
     *
     * <p>This method can be called several times, last parameter position set is remembered.</p>
     * 
     * @param value the parameter value.
     * @return the same instance (fluid API).
     * @see #addParameters(Object...)
     */
    public JEPLDALQuery addParameter(Object value);

    /**
     * Adds the specified parameter values following the parameter sequence specified with ?, ?N or :name
     * in the SQL sentence of this query.
     *
     * <p>This method can be called several times, last parameter position set is remembered.</p>
     *
     * @param values the parameter values.
     * @return the same instance (fluid API).
     * @see #addParameter(Object)
     */
    public JEPLDALQuery addParameters(Object... values);

    /**
     * Returns the parameter value defined in the required position.
     *
     * <p>If there is no parameter associated to this position a {@link JEPLException} exception is thrown.</p>
     *
     * @param position the parameter position. Starting in 1.
     * @return the parameter value.
     * @see #setParameter(int,Object)
     * @see #isBound(JEPLParameter)
     */
    public Object getParameterValue(int position);

    /**
     * Returns the parameter value associated to the specified name.
     *
     * <p>If there is no parameter associated to this position a {@link JEPLException} exception is thrown.</p>
     *
     * @param name the parameter name.
     * @return the parameter value.
     * @see #setParameter(String,Object)
     * @see #isBound(JEPLParameter)
     */
    public Object getParameterValue(String name);

    /**
     * Returns the parameter value associated to the specified parameter descriptor.
     *
     * <p>If there is no parameter associated to this position a {@link JEPLException} exception is thrown.</p>
     *
     * @param param the parameter descriptor.
     * @return the parameter value.
     * @see #getJEPLParameter(int)
     * @see #getJEPLParameter(String)
     * @see #isBound(JEPLParameter)
     */
    public <T> T getParameterValue(JEPLParameter<T> param);

    /**
     * Returns whether a value has been associated to the specified parameter.
     *
     * @param param the parameter descriptor.
     * @return true if a value has been associated.
     * @see #getParameterValue(JEPLParameter)
     */
    public boolean isBound(JEPLParameter<?> param);

    /**
     * Registers a lifecycle listener associated to this query.
     *
     * <p>Behavior is the same as {@link JEPLDAL#addJEPLListener(JEPLListener)}
     * but in this case listeners are only used in this query.
     * </p>
     *
     * @param listener the lifecycle listener object.
     * @return the same instance (fluid API).
     */
    public JEPLDALQuery addJEPLListener(JEPLListener listener);

    /**
     * Returns the the minimum number of rows affected. 
     *
     * <p>If the number of rows is lower a {@link JEPLException} exception is thrown.</p>
     * <p>A negative value means undefined.</p>
     *
     * @return the minimum number of rows affected. -1 by default.
     * @see #setStrictMinRows(int)
     */
    public int getStrictMinRows();

    /**
     * Defines the the minimum number of rows affected.
     *
     * @param value the minimum number of rows affected. A negative value means undefined.
     * @return the same instance (fluid API).
     * @see #getStrictMinRows()
     */
    public JEPLDALQuery setStrictMinRows(int value);

    /**
     * Returns the the maximum number of rows affected.
     *
     * <p>If the number of rows is upper a {@link JEPLException} exception is thrown.</p>
     * <p>A negative value means undefined.</p>
     *
     * @return the maximum number of rows affected. -1 by default.
     * @see #setStrictMaxRows(int)
     */
    public int getStrictMaxRows();

    /**
     * Defines the the maximum number of rows affected.
     *
     * @param value the maximum number of rows affected. A negative value means undefined.
     * @return the same instance (fluid API).
     * @see #getStrictMaxRows()
     */
    public JEPLDALQuery setStrictMaxRows(int value);

    /**
     * The position of the first result the query object was set to retrieve.
     * 
     * <p>Returns 1 if setFirstResult was not applied to the query object.</p>
     *
     * @return position of the first result.
     * @see #setFirstResult(int)
     */
    public int getFirstResult();

    /**
     * Set the position of the first result (row) to retrieve.
     * 
     * @param startPosition position of the first result, numbered from 1
     * @return the same instance (fluid API).
     * @see #getFirstResult()
     */
    public JEPLDALQuery setFirstResult(int startPosition);

    /**
     * The maximum number of results the query object was set to retrieve.
     *
     * <p>Returns Integer.MAX_VALUE if setMaxResults was not applied to the query object.</p>
     *
     * @return maximum number of results.
     * @see #setMaxResults(int)
     */
    public int getMaxResults();

    /**
     * Set the maximum number of results to retrieve.
     * 
     * @param maxResult maximum number of results to retrieve.
     * @return the same instance (fluid API).
     * @see #getMaxResults()
     */
    public JEPLDALQuery setMaxResults(int maxResult);

    /**
     * Executes the SQL sentence.
     *
     * @return the count of affected rows.
     */
    public int executeUpdate();

    /**
     * This method must be used to execute a SELECT statement returning a single column and is expected zero or no more than a single result row.
     *
     * <p>If more than one result is returned a {@link JEPLException} is thrown.</p>
     *
     * @param <U> the required type of the result.
     * @param returnType the required Class type of the result.
     * @return the unique result.
     */
    public <U> U getOneRowFromSingleField(Class<U> returnType);

    /**
     * This method must be used to insert a new row with an auto-generated column usually the primary key.
     *
     * <p>If more than one result is returned a {@link JEPLException} is thrown.</p>
     *
     * @param <U> the required type of the generated key.
     * @param returnType the required Class type of the generated key.
     * @return the generated key.
     */
    public <U> U getGeneratedKey(Class<U> returnType);

    /**
     * This method must be used to execute a SELECT statement,
     * it returns a connected result set ready to iterate and get database data.
     *
     * @return the connected result set of data model objects.
     */
    public JEPLResultSet getJEPLResultSet();
       
    
    /**
     * This method must be used to execute a SELECT statement, it returns a disconnected result set
     * (works with connection closed).
     *
     * @return the disconnected result set containing all of results.
     */
    public JEPLCachedResultSet getJEPLCachedResultSet();
    
    
    /**
     * Returns the generated SQL code.
     * 
     * <p>This method is only useful for methods with automatic SQL generation like {@link JEPLDAO#insert(Object)}, {@link JEPLDAO#update(Object)} and {@link JEPLDAO#delete(Object)},     * 
     * user provided SQL code is untouched.</p>
     * 
     * @return the generated SQL code.
     */
    public String getCode();
}
