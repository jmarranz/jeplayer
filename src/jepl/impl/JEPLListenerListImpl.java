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
package jepl.impl;

import jepl.JEPLConnectionListener;
import jepl.JEPLException;
import jepl.JEPLListener;
import jepl.JEPLPreparedStatementListener;
import jepl.JEPLResultSetDALListener;
import jepl.JEPLResultSetDAOListener;

/**
 *
 * @author jmarranz
 */
public class JEPLListenerListImpl
{   
    protected JEPLConnectionListener<?> connectionListener;
    protected JEPLPreparedStatementListener<?> preparedStatementListener;
    protected JEPLResultSetDALListener resultSetGenericListener;
    protected JEPLResultSetDAOListener<?> resultSetDAOListener;
    protected volatile boolean inUse = false; // La verdad es que el volatile sobra pues es para detectar errores en tiempo de desarrollo

    public JEPLListenerListImpl()
    {
    }

    private JEPLListenerListImpl(JEPLListener listener)
    {
        addJEPLListener(listener);
    }

    public static JEPLListenerListImpl getJEPLListenerList(JEPLListener listener)
    {
        if (listener == null)
            return null;
        return new JEPLListenerListImpl(listener);
    }

    @SuppressWarnings("unchecked")
	public <T> JEPLConnectionListener<T> getJEPLConnectionListener()
    {
        return (JEPLConnectionListener<T>)connectionListener;
    }

    @SuppressWarnings("unchecked")    
    public <T> JEPLPreparedStatementListener<T> getJEPLPreparedStatementListener()
    {
        return (JEPLPreparedStatementListener<T>)preparedStatementListener;
    }

    public JEPLResultSetDALListener getJEPLResultSetDALListener()
    {
        return resultSetGenericListener;
    }

    @SuppressWarnings("unchecked")
    public <T> JEPLResultSetDAOListener<T> getJEPLResultSetDAOListener()
    {
        return (JEPLResultSetDAOListener<T>)resultSetDAOListener;
    }

    public void setInUse()
    {
        this.inUse = true;
    }

    public void addJEPLListener(JEPLListener listener)
    {
        // De esta forma no necesitamos que las colecciones de listeners sean multihilo
        if (inUse)
            throw new JEPLException("Cannot register a new listener, this object is already in use");

        if (listener instanceof JEPLConnectionListener)
            this.connectionListener = (JEPLConnectionListener<?>)listener;
        else if (listener instanceof JEPLPreparedStatementListener)
            this.preparedStatementListener = (JEPLPreparedStatementListener<?>)listener;
        else if (listener instanceof JEPLResultSetDALListener)
            this.resultSetGenericListener = (JEPLResultSetDALListener)listener;
        else if (listener instanceof JEPLResultSetDAOListener)
            this.resultSetDAOListener = (JEPLResultSetDAOListener<?>)listener;
        else
            throw new JEPLException("Unknown JEPLListener " + listener);
    }

    public void removeJEPLListener(JEPLListener listener)
    {
        if (inUse)
            throw new JEPLException("Cannot unregister a listener, this object is already in use");

        if (listener instanceof JEPLConnectionListener)
        {
            if (connectionListener == listener)
                this.connectionListener = null;
        }
        else if (listener instanceof JEPLPreparedStatementListener)
        {
            if (preparedStatementListener == listener)
                this.preparedStatementListener = null;
        }
        else if (listener instanceof JEPLResultSetDALListener)
        {
            if (resultSetGenericListener == listener)
                this.resultSetGenericListener = null;
        }
        else if (listener instanceof JEPLResultSetDAOListener)
        {
            if (resultSetDAOListener == listener)
                this.resultSetDAOListener = null;
        }
        else throw new JEPLException("Unknown JEPLListener " + listener);
    }

}
