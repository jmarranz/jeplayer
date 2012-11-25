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
import java.sql.SQLException;
import java.util.Map;

import jepl.JEPLException;
import jepl.JEPLResultSet;
import jepl.JEPLStatement;
import jepl.impl.JEPLPreparedStatementImpl;
import jepl.impl.JEPLUserDataMonoThreadImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLResultSetImpl implements JEPLResultSet
{
    protected JEPLDALQueryImpl query;
    protected JEPLPreparedStatementImpl stmt;
    protected ResultSet result;
    protected JEPLUserDataMonoThreadImpl userData = new JEPLUserDataMonoThreadImpl();
    protected boolean closed = false;
    protected boolean isc3po = false;
    protected JEPLResultSetBeanInfo beanInfo;

    public JEPLResultSetImpl(JEPLDALQueryImpl query,JEPLPreparedStatementImpl stmt,ResultSet result) throws SQLException
    {
        this.query = query;
        this.stmt = stmt;
        this.result = result;

        Integer startPosition = query.getStartPosition();
        if (startPosition != null)
        {
        	stmt.moveResultSetAbsolutePosition(result, startPosition);
        }
    }

    public JEPLResultSetBeanInfo getJEPLResultSetBeanInfo(Map<String,JEPLPropertyDescriptorImpl> proertyMap) throws SQLException
    {
        if (beanInfo == null)
            this.beanInfo = new JEPLResultSetBeanInfo(proertyMap,getResultSet());
        
        return beanInfo;
    }

    public void close()
    {
        if (closed) return; // Nos ahorramos la llamada al close() por si acaso
        try
        {
            result.close();
            query.releaseJEPLPreparedStatement(stmt);
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
        finally
        {
            this.closed = true;
        }
    }

    public boolean isClosed()
    {
        if (closed) return true;

        // Posiblemente no se haya llamado explícitamente a close() pero la conexión
        // puede que se haya cerrado, o reciclado en el pool, hay que tener en cuenta que
        // nosotros mismos cerramos los statements antes de devolver al pool la conexión,
        // los cuales en teoría deberían cerrar los ResultSet pendientes.

        if (isc3po)
            return isClosedC3PO(); // Así evitamos provocar una excepción constantemente en C3PO cuando el ResultSet está abierto por culpa del bug de C3PO
        else
        {
            try
            {
                return result.isClosed();
            }
            catch(Exception ex) 
            {
                this.closed = true;
                return true;
            }
            catch(AbstractMethodError ex) // C3PO al menos 0.9.1.2 da un error AbstractMethodError (que es un Error) de no implementado isClosed
            {
                this.isc3po = true;
                return isClosedC3PO();
            }
        }
    }

    public boolean isClosedC3PO()
    {
        try
        {
            // Hacemos una llamada que no haga un cambio de estado, si falla es que
            // el ResultSet está cerrado, es una chapucilla pero en fin, quizás getRow()
            // de algún problema en algún driver JDBC pues es un método opcional en algún caso
            // quizás habría que pensar en otro método que sólo tenga sentido que funcione
            // con el ResultSet sin cerrar
            result.getRow();
            return false;
        }
        catch (SQLException ex)
        {
            this.closed = true;
            return true;
        }
    }

    public String[] getUserDataNames()
    {
        return userData.getUserDataNames();
    }

    public boolean containsName(String name)
    {
        return userData.containsName(name);
    }

    public Object getUserData(String name)
    {
        return userData.getUserData(name);
    }

    public <T> T getUserData(String name, Class<T> returnType)
    {
        return userData.getUserData(name, returnType);
    }

    public Object setUserData(String name, Object value)
    {
        return userData.setUserData(name, value);
    }

    public Object removeUserData(String name)
    {
        return userData.removeUserData(name);
    }

    public <T> T removeUserData(String name, Class<T> returnType)
    {
        return userData.removeUserData(name, returnType);
    }

    public JEPLStatement getJEPLStatement()
    {
        return stmt;
    }

    public ResultSet getResultSet()
    {
        return result;
    }

}
