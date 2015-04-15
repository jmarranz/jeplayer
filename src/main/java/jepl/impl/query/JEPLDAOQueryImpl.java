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
import java.util.LinkedList;
import java.util.List;
import jepl.JEPLDAOQuery;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLResultSetDAO;
import jepl.JEPLResultSetDAOListener;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDAOImpl;
import jepl.impl.JEPLPreparedStatementImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;
import jepl.impl.JEPLTaskOneExecutionImpl;

/**
 *
 * @author jmarranz
 * @param <T>
 */
public class JEPLDAOQueryImpl<T> extends JEPLDALQueryImpl implements JEPLDAOQuery<T>
{
    public JEPLDAOQueryImpl(JEPLDAOImpl<T> dal,String sqlOriginal)
    {
        super(dal,sqlOriginal);
    }

    @SuppressWarnings("unchecked")
    public JEPLDAOImpl<T> getJEPLDAOImpl()
    {
        return (JEPLDAOImpl<T>)dal;
    }
    
    @Override
    public JEPLDAOQuery<T> addJEPLListener(JEPLListener listener)
    {
        super.addJEPLListener(listener);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setParameter(int position,Object value)
    {
        super.setParameter(position,value);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setParameter(String name,Object value)
    {
        super.setParameter(name, value);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> addParameter(Object value)
    {
        super.addParameter(value);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> addParameters(Object... values)
    {
        super.addParameters(values);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setStrictMinRows(int value)
    {
        super.setStrictMinRows(value);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setStrictMaxRows(int value)
    {
        super.setStrictMaxRows(value);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setFirstResult(int startPosition)
    {
        super.setFirstResult(startPosition);
        return this;
    }

    @Override
    public JEPLDAOQuery<T> setMaxResults(int maxResult)
    {
        super.setMaxResults(maxResult);
        return this;
    }

    public JEPLResultSetDAOListener<T> getJEPLResultSetDAOListener()
    {
        // El retorno no puede ser nulo, necesitamos un listener para saber como
        // recoger los resultados

        JEPLResultSetDAOListener<T> listener;
        if (listenerList != null)
        {
            listener = listenerList.getJEPLResultSetDAOListener();
            if (listener != null)
                return listener;
        }

        listener = getJEPLDAOImpl().getJEPLListenerList().getJEPLResultSetDAOListener();
        if (listener != null)
            return listener;

        listener = getJEPLDataSourceImpl().getJEPLListenerList().getJEPLResultSetDAOListener();
        if (listener == null) // Es necesario que haya uno
            throw new JEPLException("Missing parameter implementing " + JEPLResultSetDAOListener.class + " or a listener registered on DAL/DAO or data source");
        return listener;
    }

    @Override
    public List<T> getResultList()
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<List<T>> task = new JEPLTaskOneExecWithConnectionImpl<List<T>>()
                {
                    @Override
                    public List<T> execInherit() throws Exception
                    {
                        return getResultList(getJEPLConnection());
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getResultList(conWrap);
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

    public List<T> getResultList(final JEPLConnectionImpl conWrap) throws Exception
    {
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);
        try
        {
            JEPLTaskOneExecutionImpl<List<T>> taskWrap = new JEPLTaskOneExecutionImpl<List<T>>()
            {
                @Override
                protected List<T> execInherit() throws Exception
                {
                    ResultSet rs = stmt.getPreparedStatement().executeQuery();
                    try
                    {
                        final JEPLResultSetDefaultImpl jrs = new JEPLResultSetDefaultImpl(JEPLDAOQueryImpl.this,stmt,rs);
                        final JEPLResultSetDAOListener<T> listener = getJEPLResultSetDAOListener();

                        JEPLTaskOneExecutionImpl<List<T>> taskWrapLevel2 = new JEPLTaskOneExecutionImpl<List<T>>()
                        {
                            @Override
                            protected List<T> execInherit() throws Exception
                            {
                                return getResultList(jrs,listener);
                            }
                        };

                        listener.setupJEPLResultSet(jrs, taskWrapLevel2);

                        if (taskWrapLevel2.isExecuted())
                            return taskWrapLevel2.getResult();
                        else
                            return taskWrapLevel2.exec();
                    }
                    finally
                    {
                        rs.close();
                    }
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    public List<T> getResultList(JEPLResultSetImpl jrs,JEPLResultSetDAOListener<T> listener) throws Exception
    {
        List<T> objList = new LinkedList<T>();
        ResultSet rs = jrs.getResultSet();
        if (firstRow(rs))
        {     
            do
            {
                T obj = listener.createObject(jrs);
                if (obj != null) // El ser null es como un "continue", útil para filtrar objetos
                {
                    listener.fillObject(obj,jrs);
                    objList.add(obj);
                }
                if (jrs.isStopped()) break;
            }
            while(nextRow(rs,objList.size()));
        }
        checkNumOfReturnedRows(objList.size());
        
        return objList;
    }

    @Override
    public T getSingleResult()
    {
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<T> task = new JEPLTaskOneExecWithConnectionImpl<T>()
                {
                    @Override
                    public T execInherit() throws Exception
                    {
                        return getSingleResult(getJEPLConnection());
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getSingleResult(conWrap);
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

    public T getSingleResult(final JEPLConnectionImpl conWrap) throws Exception
    {
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);
        try
        {
            JEPLTaskOneExecutionImpl<T> taskWrap = new JEPLTaskOneExecutionImpl<T>()
            {
                @Override
                protected T execInherit() throws Exception
                {
                    ResultSet rs = stmt.getPreparedStatement().executeQuery();
                    try
                    {
                        final JEPLResultSetDefaultImpl jrs = new JEPLResultSetDefaultImpl(JEPLDAOQueryImpl.this,stmt,rs);
                        final JEPLResultSetDAOListener<T> listener = getJEPLResultSetDAOListener();
                       // listener no es nulo, el task no es opcional en este caso
                        JEPLTaskOneExecutionImpl<T> taskWrapLevel2 = new JEPLTaskOneExecutionImpl<T>()
                        {
                            @Override
                            protected T execInherit() throws Exception
                            {
                                return getSingleResult(jrs,listener);
                            }
                        };
                        listener.setupJEPLResultSet(jrs, taskWrapLevel2);

                        if (taskWrapLevel2.isExecuted())
                            return taskWrapLevel2.getResult();
                        else
                            return taskWrapLevel2.exec();
                    }
                    finally
                    {
                        rs.close();
                    }
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            releaseJEPLPreparedStatement(stmt);
        }
    }

    public T getSingleResult(JEPLResultSetImpl jrs,JEPLResultSetDAOListener<T> listener) throws Exception
    {
        // A diferencia de JPA permitimos que no haya resultado (nulo), es MUCHO más práctico
        T obj = null;
        ResultSet rs = jrs.getResultSet();
        if (firstRow(rs))
        {
            obj = listener.createObject(jrs);
            if (obj != null) // El ser null es como un "continue", útil como filtro añadido del objeto resultante
                listener.fillObject(obj,jrs);

            if (!jrs.isStopped())
                if (nextRow(rs,1))
                    throw new JEPLException("Expected only a single (or none) result");

            checkNumOfReturnedRows(1); // Por si el usuario espera 0 estricto
        }
        else
        {
            checkNumOfReturnedRows(0); // Por si el usuario quiere 1 estricto
        }
        return obj;
    }

    @Override
    public JEPLResultSetDAO<T> getJEPLResultSetDAO()
    {
        JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
        if (conWrap == null)
        {
            // En este caso a través de JEPLResultSetDAO "sacamos" un ResultSet "vivo"
            // por lo que no podemos crear un JEPLTask auxiliar y ejecutarla, pues
            // la ejecución está diseñada para obtener una conexión y realizar un ciclo completo
            // y no queremos perder el control del ciclo de vida de la conexión, transacciones etc
            // para eso está JDBC plano
            throw new JEPLException("This method requires a task to be executed");
        }
        else
        {
            try
            {

                return getJEPLResultSetDAO(conWrap);
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
    }

    public JEPLResultSetDAO<T> getJEPLResultSetDAO(final JEPLConnectionImpl conWrap) throws Exception
    {
        final JEPLPreparedStatementImpl stmt = createJEPLPrepareStatement(conWrap);

        try
        {
            JEPLTaskOneExecutionImpl<JEPLResultSetDAO<T>> taskWrap = new JEPLTaskOneExecutionImpl<JEPLResultSetDAO<T>>()
            {
                @Override
                protected JEPLResultSetDAO<T> execInherit() throws Exception
                {
                    ResultSet rs = stmt.getPreparedStatement().executeQuery();

                    try
                    {
                        final JEPLResultSetDAOListener<T> listener = getJEPLResultSetDAOListener();
                        final JEPLResultSetDAOImpl<T> jrs = new JEPLResultSetDAOImpl<T>(JEPLDAOQueryImpl.this,stmt,rs,listener);

                        JEPLTaskOneExecutionImpl<JEPLResultSetDAO<T>> taskWrapLevel2 = new JEPLTaskOneExecutionImpl<JEPLResultSetDAO<T>>()
                        {
                            @Override
                            protected JEPLResultSetDAO<T> execInherit() throws Exception
                            {
                                // Por pura coherencia
                                return jrs;
                            }
                        };

                        listener.setupJEPLResultSet(jrs, taskWrapLevel2);

                        if (taskWrapLevel2.isExecuted())
                            return taskWrapLevel2.getResult();
                        else
                            return taskWrapLevel2.exec();
                    }
                    finally
                    {
                        // EN ESTE CASO NO hacemos el finally { rs.close() } pues es el único
                        // caso en el que sacamos "afuera" el ResultSet sin cerrar
                        // y por supuesto NO chequeamos el número de resultados
                        // porque llamar a size() supondría cargar todos los resultados
                        // Y ESO es justamente lo que NO queremos (carga bajo demanda).
                    }
                }
            };
            return executeQuery(stmt,taskWrap);
        }
        finally
        {
            // NO LLAMAMOS a releaseJEPLPreparedStatement(stmt); porque el PreparedStatement
            // no se libera hasta que el ResultSet dentro de JEPLResultSetDAO se cierre
        }
    }
}
