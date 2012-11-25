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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jepl.JEPLException;
import jepl.JEPLPreparedStatement;

/**
 *
 * @author jmarranz
 */
public abstract class JEPLPreparedStatementImpl extends JEPLStatementImpl implements JEPLPreparedStatement
{
    protected String key;
    
    public JEPLPreparedStatementImpl(JEPLConnectionImpl jcon,JEPLDALImpl dalDeleg,PreparedStatement stmt,String key) throws SQLException
    {
        super(jcon,dalDeleg,stmt);
        this.key = key;
    }

    public PreparedStatement getPreparedStatement()
    {
        return (PreparedStatement)stmt;
    }

    public void setParameters(Object[] paramList) throws SQLException
    {
        PreparedStatement stmt = getPreparedStatement();
        for(int i = 0; i < paramList.length; i++)
        {
            Object par = paramList[i];
            setObject(stmt,i + 1,par); // EMPIEZA EN 1 !!
        }
    }

    protected void setObject(PreparedStatement stmt,int index,Object param) throws SQLException
    {
    	stmt.setObject(index,param);    	
    }
    
    public String getKey()
    {
        return key;
    }

    public ResultSet executeUpdateGetGeneratedKeys(String sqlJDBC) throws Exception
    { 	
    	// sqlJDBC no se usa aqui, es para la sobrecarga de Android
        PreparedStatement stmt = getPreparedStatement();
        int count = stmt.executeUpdate();
        if (count == 0) throw new JEPLException("No row has been inserted"); // Si hay transaccion se abortara la operacion
        if (count > 1) throw new JEPLException("More than a row has been inserted");

        return stmt.getGeneratedKeys();
    }
    
    public void moveResultSetAbsolutePosition(ResultSet result,int position) throws SQLException
    {
    	// La razon de este metodo tan simple y en JEPLPreparedStatement es que es sobreescrito en Android
    	result.absolute(position);
    }
    
    public boolean isExecuteUpdateReturnCorrect()
    {
    	// La razon de este metodo es que se redefine en Android 
    	return true;
    }
}
