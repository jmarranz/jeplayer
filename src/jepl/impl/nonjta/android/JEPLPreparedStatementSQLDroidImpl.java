package jepl.impl.nonjta.android;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jepl.JEPLException;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLPreparedStatementImpl;

public class JEPLPreparedStatementSQLDroidImpl extends JEPLPreparedStatementImpl 
{
	public JEPLPreparedStatementSQLDroidImpl(JEPLNonJTAConnectionSQLDroidImpl jcon,JEPLDALImpl dal,PreparedStatement stmt,String key) throws SQLException
	{
		super(jcon,dal,stmt,key);
	}
	
	public JEPLNonJTAConnectionSQLDroidImpl getJEPLNonJTAConnectionSQLDroid()
	{
		return (JEPLNonJTAConnectionSQLDroidImpl)jcon;
	}
	
	@Override
    protected void setObject(PreparedStatement stmt,int column,Object param) throws SQLException
    {
        // Llamamos al método más apropiado de acuerdo con el tipo de datos esperado
    	// algunos de ellos no están implementados pero los llamamos por sistema
    	// Obviamente no puede ser un primitivo
        
        if (param == null)
        {
        	stmt.setNull(column, -1); // El tipo da igual
        	return;
        }
        
    	Class<?> targetType = param.getClass();
    	
        if (Number.class.isAssignableFrom(targetType))
        {
            if (targetType.equals(Byte.class))
                stmt.setByte(column,(Byte)param);
            else if (targetType.equals(Short.class))
            	stmt.setShort(column,(Short)param);
            else if (targetType.equals(Integer.class))
            	stmt.setInt(column,(Integer)param);
            else if (targetType.equals(Long.class))
            	stmt.setLong(column,(Long)param);
            else if (targetType.equals(Float.class))
            	stmt.setFloat(column,(Float)param);
            else if (targetType.equals(Double.class))
            	stmt.setDouble(column,(Double)param);
            else if (targetType.equals(BigDecimal.class))
                stmt.setBigDecimal(column,(BigDecimal)param);
            else
            	throw new JEPLException("Not supported data type:" + targetType); // Puede ser BigInteger
        }
        else if (targetType.equals(Boolean.class))
        	stmt.setBoolean(column,(Boolean)param);
        else if (targetType.equals(Character.class))
        	stmt.setString(column,new String(param.toString()));
        else if (targetType.equals(String.class))
        	stmt.setString(column,(String)param);
        else if (targetType.equals(java.sql.Date.class))
        	stmt.setDate(column,(java.sql.Date)param);
        else if (targetType.equals(java.sql.Time.class))
        	stmt.setTime(column,(java.sql.Time)param);
        else if (targetType.equals(java.sql.Timestamp.class))
        	stmt.setTimestamp(column,(java.sql.Timestamp)param);
        else
        	throw new JEPLException("Not supported data type:" + targetType);
 	
    }	
    
    @Override
    public ResultSet executeUpdateGetGeneratedKeys(String sqlJDBC) throws Exception
    { 	
		// SQLDroid no soporta getGeneratedKeys
        PreparedStatement stmt = getPreparedStatement();
        stmt.executeUpdate();
        // No chequeamos que el resultado de executeUpdate() sea 1 pues SQLDroid siempre devuelve 0
		// nos tenemos que fiar.
		Statement stmtLastId = jcon.getConnection().createStatement();
		ResultSet res = stmtLastId.executeQuery("SELECT LAST_INSERT_ROWID()");
		// La alternativa sería: select max(id) from sometable
		// pero exige conocer el nombre del atributo id y la tabla
		return res;
    }    
    
    @Override
    public void moveResultSetAbsolutePosition(ResultSet result,int position) throws SQLException
    {
    	getJEPLNonJTAConnectionSQLDroid().moveResultSetAbsolutePosition(result,position);
    }    
    
    @Override    
    public boolean isExecuteUpdateReturnCorrect()
    {
    	// executeUpdate() siempre devuelve 0 en SQLDroid
    	return false;
    }    
}
