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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jepl.JEPLCachedResultSet;
import jepl.JEPLDALQuery;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLParameter;
import jepl.JEPLPreparedStatementListener;
import jepl.JEPLResultSetDALListener;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLDataSourceImpl;
import jepl.impl.JEPLListenerListImpl;
import jepl.impl.JEPLPreparedStatementImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;
import jepl.impl.JEPLTaskOneExecutionImpl;
import jepl.impl.JEPLUtilImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLDALQueryImpl implements JEPLDALQuery
{   
    protected JEPLDALImpl dal;
    protected JEPLQueryParsedImpl queryParsed;
    protected ArrayList<JEPLParameterImpl<Object>> paramsByJDBCPosition;
    protected Map<String,JEPLParameterValueNamedImpl<Object>> paramsByName;
    protected JEPLListenerListImpl listenerList;
    protected int strictMinRows = -1;
    protected int strictMaxRows = -1;
    protected int currentPos = 1;
    protected Integer startPosition = null;
    protected Integer maxRows = null;

    public JEPLDALQueryImpl(JEPLDALImpl dal,String sqlOriginal)
    {
        this.dal = dal;
        this.queryParsed = dal.getJEPLDataSourceImpl().getJEPLBootImpl().getJEPLQueryParsedCache().getJEPLQueryParsed(sqlOriginal);

        if (queryParsed.getParamsByJDBCPosition() != null)
        {
            ArrayList<JEPLParameterDecImpl> paramListByJDBCPos = queryParsed.getParamsByJDBCPosition();

            this.paramsByJDBCPosition = new ArrayList<JEPLParameterImpl<Object>>();

            for(int i = 0; i < paramListByJDBCPos.size(); i++)
            {
                JEPLParameterImpl<Object> param;

                JEPLParameterDecImpl paramDec = paramListByJDBCPos.get(i);
                if (paramDec instanceof JEPLParameterDecWithNumberImpl)
                {
                    param = new JEPLParameterWithNumberImpl<Object>((JEPLParameterDecWithNumberImpl)paramDec);
                }
                else if (paramDec instanceof JEPLParameterDecWithNameImpl)
                {
                    // Permitimos DELIBERADAMENTE repetir varios :name
                    JEPLParameterDecWithNameImpl paramDecWithName = (JEPLParameterDecWithNameImpl)paramDec;
                    String name = paramDecWithName.getName();

                    if (paramsByName == null)
                        this.paramsByName = new HashMap<String,JEPLParameterValueNamedImpl<Object>>();
                    JEPLParameterValueNamedImpl<Object> paramValue = (JEPLParameterValueNamedImpl<Object>)paramsByName.get(name);
                    if (paramValue == null)
                    {
                        paramValue = new JEPLParameterValueNamedImpl<Object>(name);
                        paramsByName.put(name,paramValue);
                    }
                    
                    param = new JEPLParameterWithNameImpl<Object>(paramDecWithName,paramValue);
                }
                else
                {
                    throw new JEPLException("INTERNAL ERROR");
                }

                paramsByJDBCPosition.add(param);
            }
        }
    }

    public String getSQLJDBC()
    {
    	return queryParsed.getSQLJDBC();
    }
    
    public JEPLListenerListImpl getJEPLListenerList()
    {
        if (listenerList == null) listenerList = new JEPLListenerListImpl();
        return listenerList;
    }

    public JEPLDataSourceImpl getJEPLDataSourceImpl()
    {
        return dal.getJEPLDataSourceImpl();
    }


    public JEPLParameter<?> getJEPLParameter(int position)
    {
        return getJEPLParameterWithNumber(position);
    }

    public <T> JEPLParameter<T> getJEPLParameter(int position,Class<T> type)
    {
        return getJEPLParameterWithNumber(position,type);
    }

    public JEPLParameter<?> getJEPLParameter(String name)
    {
        return getJEPLParameterValueNamed(name).getJEPLParameterWithName();
    }

    public <T> JEPLParameter<T> getJEPLParameter(String name,Class<T> type)
    {
        return getJEPLParameterValueNamed(name,type).getJEPLParameterWithName();
    }

    public JEPLDALQuery setParameter(int position, Object value)
    {
        JEPLParameterWithNumberImpl<Object> param = getJEPLParameterWithNumber(position);
        param.setValue(value);
        return this;
    }

    public JEPLDALQuery setParameter(String name,Object value)
    {
        JEPLParameterValueNamedImpl<Object> paramValue = getJEPLParameterValueNamed(name);
        paramValue.setValue(value);
        return this;
    }

    public Object getParameterValue(int position)
    {
        JEPLParameterWithNumberImpl<Object> param = getJEPLParameterWithNumber(position);
        return param.getValue();
    }

    public Object getParameterValue(String name)
    {
        JEPLParameterValueNamedImpl<Object> paramValue = getJEPLParameterValueNamed(name);
        return paramValue.getValue();
    }

    public <T> T getParameterValue(JEPLParameter<T> param)
    {
        return ((JEPLParameterImpl<T>)param).getValue();
    }

    public JEPLDALQuery addParameter(Object value)
    {
        setParameter(currentPos,value);
        currentPos++;
        return this;
    }

    public JEPLDALQuery addParameters(Object... values)
    {
        for(int i = 0; i < values.length; i++)        
            addParameter(values[i]);
        return this;
    }

    public boolean isBound(JEPLParameter<?> param)
    {
        return ((JEPLParameterImpl<?>)param).isBound();
    }

    public JEPLParameterWithNumberImpl<Object> getJEPLParameterWithNumber(int position)
    {
        return getJEPLParameterWithNumber(position,Object.class);
    }

    public <T> JEPLParameterWithNumberImpl<T> getJEPLParameterWithNumber(int position,Class<T> type)
    {
        // Si el índice está fuera de rango dará el error pertinente
        if (paramsByJDBCPosition == null)
            throw new JEPLException("There is no parameter in SQL sentence");
        @SuppressWarnings("unchecked")
		JEPLParameterImpl<T> param = (JEPLParameterImpl<T>)paramsByJDBCPosition.get(position - 1); // Quitamos 1 pues el Map es en base 0
        if (param instanceof JEPLParameterWithNameImpl)
            throw new JEPLException("There is no standard ? or numbered parameter ?N in this position " + position); // Aunque position coincide con la positición JDBC nos interesa devolver el parámetro que está numéricamente posicionado (implícita o explícitamente)
        return (JEPLParameterWithNumberImpl<T>)param;
    }

    public JEPLParameterValueNamedImpl<Object> getJEPLParameterValueNamed(String name)
    {
        return getJEPLParameterValueNamed(name,Object.class);
    }

    public <T> JEPLParameterValueNamedImpl<T> getJEPLParameterValueNamed(String name,Class<T> type)
    {
        // Si el índice está fuera de rango dará el error pertinente
        if (paramsByName == null)
            throw new JEPLException("There is no named parameter in SQL sentence");
        @SuppressWarnings("unchecked")
		JEPLParameterValueNamedImpl<T> paramValue = (JEPLParameterValueNamedImpl<T>)paramsByName.get(name);
        if (paramValue == null)
            throw new JEPLException("There is no named parameter with name " + name); 
        return (JEPLParameterValueNamedImpl<T>)paramValue;
    } 

    public Object[] getParameterValues()
    {
        if (paramsByJDBCPosition == null) return null;
        Object[] valueList = new Object[paramsByJDBCPosition.size()];
        for(int i = 0; i < paramsByJDBCPosition.size(); i++)
        {
            JEPLParameterImpl<Object> param = paramsByJDBCPosition.get(i);
            Object value = param.getValue();
            valueList[i] = value;
        }
        return valueList;
    }

    public JEPLDALQuery addJEPLListener(JEPLListener listener)
    {
        getJEPLListenerList().addJEPLListener(listener);
        return this;
    }

    public int getStrictMinRows()
    {
        return strictMinRows;
    }

    public JEPLDALQuery setStrictMinRows(int value)
    {
        this.strictMinRows = value;
        return this;
    }

    public int getStrictMaxRows()
    {
        return strictMaxRows;
    }

    public JEPLDALQuery setStrictMaxRows(int value)
    {
        this.strictMaxRows = value;
        return this;
    }

    public Integer getStartPosition()
    {
    	return startPosition;
    }
    
    public int getFirstResult()
    {
        if (startPosition != null) return startPosition;
        else return 1;
    }

    public JEPLDALQuery setFirstResult(int startPosition)
    {
        this.startPosition = startPosition;
        return this;
    }

    public int getMaxResults()
    {
        if (maxRows != null) return maxRows;
        return Integer.MAX_VALUE;
    }

    public JEPLDALQuery setMaxResults(int maxResult)
    {
        this.maxRows = maxResult;
        return this;
    }

    public boolean isMaxRowsAchieved(int count)
    {
        return (maxRows != null && maxRows >= 0 && count == maxRows);
    }

    public void checkNumOfReturnedRows(int count)
    {
        // Si está dentro de una transacción y hay error se hará rollback la operación
        if (strictMinRows >= 0)
        {
            if (count < strictMinRows)
                throw new JEPLException("Less than " + strictMinRows + " rows processed");
        }
        if (strictMaxRows >= 0)
        {
            if (count > strictMaxRows)
                throw new JEPLException("More than " + strictMinRows + " rows processed");
        }
    }

    public boolean firstRow(ResultSet rs) throws SQLException
    {
        if (isMaxRowsAchieved(0)) return false;
        if (0 != rs.getRow())
            return true; // Seguramente ha habido una llamada a ResultSet.absolute(int), no llamamos a next() pues nos saltamos la row actual en la que nos hemos posicionado y no puede llamarse a absolute(0) con el fin de que next() nos situe en la pos 1
        return rs.next();
    }

    public boolean nextRow(ResultSet rs,int count) throws SQLException
    {
        if (isMaxRowsAchieved(count)) return false;
        return rs.next();
    }

    public <T> T executeQuery(JEPLPreparedStatementImpl jstmt,JEPLTaskOneExecutionImpl<T> taskWrap) throws Exception
    {

        JEPLPreparedStatementListener<T> stmtListener = this.<T>getJEPLPreparedStatementListener();
        if (stmtListener != null)
            stmtListener.setupJEPLPreparedStatement(jstmt,taskWrap);

        if (taskWrap.isExecuted())
            return taskWrap.getResult();
        else
            return taskWrap.exec();
    }

    public <T> T execWithTask(JEPLTaskOneExecWithConnectionImpl<T> task) throws Exception
    {
        // Es el caso de necesitar un task porque se quiere ejecutar una queryParsed directamente sin task
        // Paramos el paramListener únicamente por el caso singular de suministrar
        // un JEPLConnectionListener como parámetro de la queryParsed en este caso de de llamada
        // directa sin task.
        return getJEPLDataSourceImpl().execInternal(task,dal,listenerList);
    }
    
    public <T> JEPLPreparedStatementListener<T> getJEPLPreparedStatementListener()
    {
        JEPLPreparedStatementListener<T> listener;
        if (listenerList != null)
        {
            listener = listenerList.getJEPLPreparedStatementListener();
            if (listener != null)
                return listener;
        }
        
        listener = dal.getJEPLListenerList().getJEPLPreparedStatementListener();
        if (listener != null)
            return listener;

        listener = getJEPLDataSourceImpl().getJEPLListenerList().getJEPLPreparedStatementListener();
        // Puede ser finalmente nulo, no es obligatorio
        return listener;
    }

    public void releaseJEPLPreparedStatement(JEPLPreparedStatementImpl stmt) throws SQLException
    {
        stmt.getJEPLConnectionImpl().releaseJEPLPreparedStatement(stmt);
    }

    public JEPLPreparedStatementImpl createJEPLPrepareStatement(JEPLConnectionImpl conWrap) throws SQLException
    {
        return createJEPLPrepareStatement(conWrap,false);
    }
    
    public JEPLPreparedStatementImpl createJEPLPrepareStatement(JEPLConnectionImpl conWrap,boolean generatedKeys) throws SQLException
    {
        return dal.createJEPLPrepareStatement(conWrap,generatedKeys,getSQLJDBC(),getParameterValues());
    }

    public JEPLResultSetDALListener getJEPLResultSetDALListener()
    {
        // El retorno no puede ser nulo, necesitamos un listener para saber como
        // recoger los resultados
        JEPLResultSetDALListener listener;
        if (listenerList != null)
        {
            listener = listenerList.getJEPLResultSetDALListener();
            if (listener != null)
                return listener;
        }

        listener = dal.getJEPLListenerList().getJEPLResultSetDALListener();
        if (listener != null)
            return listener;

        listener = getJEPLDataSourceImpl().getJEPLListenerList().getJEPLResultSetDALListener();
        // puede ser nulo
        return listener;
    }

    @Override
    public int executeUpdate()
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<Integer> task = new JEPLTaskOneExecWithConnectionImpl<Integer>()
                {
                    public Integer execInherit() throws Exception
                    {
                        return executeUpdate(getJEPLConnection());
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return executeUpdate(conWrap);
            }
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public int executeUpdate(JEPLConnectionImpl conWrap) throws Exception
    {
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);
        try
        {
            JEPLTaskOneExecutionImpl<Integer> taskWrap = new JEPLTaskOneExecutionImpl<Integer>()
            {
                @Override
                protected Integer execInherit() throws Exception
                {
                    int count = stmt.getPreparedStatement().executeUpdate();
                    if (stmt.isExecuteUpdateReturnCorrect())
                    	checkNumOfReturnedRows(count); // Si el valor devuelto count es siempre cero como en el caso de SQLDroid no tiene sentido el chequeo y fastidia la portabilidad
                    return count;
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    @Override
    public <U> U getOneRowFromSingleField(final Class<U> returnType)
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<U> task = new JEPLTaskOneExecWithConnectionImpl<U>()
                {
                    public U execInherit() throws Exception
                    {
                        return getOneRowFromSingleField(getJEPLConnection(),returnType);
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getOneRowFromSingleField(conWrap,returnType);
            }
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }
    
    public <U> U getOneRowFromSingleField(JEPLConnectionImpl conWrap,final Class<U> returnType) throws Exception
    {
        // Este método es interesante por ejemplo para hacer SELECT COUNT(*) y similares
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);

        try
        {
            JEPLTaskOneExecutionImpl<U> taskWrap = new JEPLTaskOneExecutionImpl<U>()
            {
                @Override
                protected U execInherit() throws Exception
                {
                    return getOneRowFromSingleField(stmt,returnType);
                }
            };           
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    public <U> U getOneRowFromSingleField(JEPLPreparedStatementImpl jstmt,final Class<U> returnType) throws Exception
    {
        ResultSet rs = jstmt.getPreparedStatement().executeQuery();

        try
        {
            return getOneOrNoneResultRowOneField(rs,jstmt,returnType);
        }
        finally
        {
            rs.close();
        }
    }

    @Override
    public <U> U getGeneratedKey(final Class<U> returnType)
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<U> task = new JEPLTaskOneExecWithConnectionImpl<U>()
                {
                    public U execInherit() throws Exception
                    {
                        return getGeneratedKey(getJEPLConnection(),returnType);
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getGeneratedKey(conWrap,returnType);
            }
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public <U> U getGeneratedKey(JEPLConnectionImpl conWrap,final Class<U> returnType) throws Exception
    {
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap,true);

        try
        {
            JEPLTaskOneExecutionImpl<U> taskWrap = new JEPLTaskOneExecutionImpl<U>()
            {
                @Override
                protected U execInherit() throws Exception
                {
                    return getGeneratedKey(stmt,returnType);
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    public <U> U getGeneratedKey(JEPLPreparedStatementImpl jstmt,final Class<U> returnType) throws Exception
    { 	
        ResultSet rs = jstmt.executeUpdateGetGeneratedKeys(getSQLJDBC());
        try
        {
            return getOneOrNoneResultRowOneField(rs,jstmt,returnType);
        }
        finally
        {
            rs.close();
        }
    }

    public <U> U getOneOrNoneResultRowOneField(ResultSet rs,JEPLPreparedStatementImpl jstmt,final Class<U> returnType) throws Exception
    {
        final JEPLResultSetDefaultImpl jrs = new JEPLResultSetDefaultImpl(this,jstmt,rs);

        final JEPLResultSetDALListener resultSetListener = getJEPLResultSetDALListener();
        // resultSetListener puede ser nulo, no es obligatorio
        if (resultSetListener != null)
        {
            JEPLTaskOneExecutionImpl<U> task = new JEPLTaskOneExecutionImpl<U>()
            {
                @Override
                protected U execInherit() throws Exception
                {
                    return getOneOrNoneResultRowOneField(jrs,returnType,resultSetListener);
                }
            };
            resultSetListener.setupJEPLResultSet(jrs, task);

            if (task.isExecuted())
                return task.getResult();
            else
                return task.exec();
        }
        else
        {
            return getOneOrNoneResultRowOneField(jrs,returnType,null);
        }
    }

    public <U> U getOneOrNoneResultRowOneField(JEPLResultSetImpl jrs,Class<U> returnType,JEPLResultSetDALListener listener) throws Exception
    {
        U obj = null;
        ResultSet rs = jrs.getResultSet();
        if (firstRow(rs))
        {
            if (listener != null)
            {
                obj = listener.getValue( 1, returnType, jrs);
            }
            else
            {
                Object resObj = JEPLUtilImpl.getResultSetColumnObject(rs, 1, returnType);
                obj = dal.cast(resObj,returnType);
            }

            if (nextRow(rs,1))
                throw new JEPLException("Only supported one (or none) result");

            checkNumOfReturnedRows( 1 ); // Por si el usuario espera 0 estricto
        }
        else
        {
            checkNumOfReturnedRows( 0 ); // Por si el usuario quiere 1 estricto
        }

        return obj;
    }

    @Override
    public JEPLCachedResultSet getJEPLCachedResultSet()
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<JEPLCachedResultSet> task = new JEPLTaskOneExecWithConnectionImpl<JEPLCachedResultSet>()
                {
                    @Override
                    public JEPLCachedResultSet execInherit() throws Exception
                    {
                        return getJEPLCachedResultSet(getJEPLConnection());
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getJEPLCachedResultSet(conWrap);
            }
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public JEPLCachedResultSet getJEPLCachedResultSet(JEPLConnectionImpl conWrap) throws Exception
    {
        // Este método es interesante por ejemplo para hacer SELECT COUNT(*) y similares

        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);

        try
        {
            // No ponemos como tipo JEPLCachedResultSetImpl porque dicho tipo
            // será "visible" para el parámetro task de JEPLPreparedStatementListener
            JEPLTaskOneExecutionImpl<JEPLCachedResultSet> taskWrap =
                    new JEPLTaskOneExecutionImpl<JEPLCachedResultSet>()
            {
                @Override
                protected JEPLCachedResultSet execInherit() throws Exception
                {
                    return getJEPLCachedResultSet(stmt);
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    public JEPLCachedResultSet getJEPLCachedResultSet(JEPLPreparedStatementImpl stmt) throws Exception
    {
        ResultSet rs = stmt.getPreparedStatement().executeQuery();
        try
        {
            final JEPLResultSetDefaultImpl jrs = new JEPLResultSetDefaultImpl(this,stmt,rs);
            final JEPLResultSetDALListener resultSetListener = getJEPLResultSetDALListener();
            // resultSetListener puede ser nulo, no es obligatorio
            if (resultSetListener != null)
            {
                JEPLTaskOneExecutionImpl<JEPLCachedResultSet> task = new JEPLTaskOneExecutionImpl<JEPLCachedResultSet>()
                {
                    @Override
                    protected JEPLCachedResultSet execInherit() throws Exception
                    {
                        return getJEPLCachedResultSet(jrs,resultSetListener);
                    }
                };
                resultSetListener.setupJEPLResultSet(jrs, task);

                if (task.isExecuted())
                    return task.getResult();
                else
                    return task.exec();
            }
            else
            {
                return getJEPLCachedResultSet(jrs,null);
            }
        }
        finally
        {
            rs.close();
        }
    }

    public JEPLCachedResultSet getJEPLCachedResultSet(JEPLResultSetImpl jrs,JEPLResultSetDALListener resultSetListener) throws Exception
    {           
        ResultSet rs = jrs.getResultSet();
        ResultSetMetaData metadata = rs.getMetaData();
        int ncols = metadata.getColumnCount();
        String[] colLabels = new String[ncols];
        for(int i = 0; i < ncols; i++)
            colLabels[i] = metadata.getColumnLabel(i + 1); // Empieza en 1

        ArrayList<Object[]> values = new ArrayList<Object[]>();

        if (firstRow(rs))
        {
            do
            {
                Object[] rowValues = new Object[ncols];
                for(int col = 0; col < ncols; col++)
                {
                    int columnIndex = col + 1; // Empieza en 1
                    if (resultSetListener != null)
                    {
                        resultSetListener.getValue(columnIndex,Object.class, jrs);
                    }
                    else
                    {
                        rowValues[col] = rs.getObject(columnIndex); // Empieza en 1
                    }
                }
                values.add(rowValues);
            }
            while(nextRow(rs,values.size()));
        }

        checkNumOfReturnedRows(values.size());

        return new JEPLCachedResultSetImpl(colLabels,values);
    }

}
