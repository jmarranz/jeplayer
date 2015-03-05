package jepl.impl.nonjta.android;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jepl.JEPLException;
import jepl.impl.JEPLDALImpl;
import jepl.impl.JEPLPreparedStatementImpl;
import jepl.impl.nonjta.JEPLNonJTAConnectionImpl;

public class JEPLNonJTAConnectionSQLDroidImpl extends JEPLNonJTAConnectionImpl 
{
	protected Field androidCursorField;
	protected Method androidMoveToPositionMethod;	
	
    public JEPLNonJTAConnectionSQLDroidImpl(JEPLNonJTADataSourceAndroidImpl ds,Connection con) throws SQLException
    {
        super(ds,con);
    }
    
    protected JEPLPreparedStatementImpl createJEPLPreparedStatement(JEPLDALImpl dal,PreparedStatement stmt,String key) throws SQLException
    {
    	return new JEPLPreparedStatementSQLDroidImpl(this,dal,stmt,key);
    }    
        
    @Override
    protected PreparedStatement prepareStatement(String sql,int autoGeneratedKeys) throws SQLException
    {
		// No está soportado prepareStatement(sql, autoGeneratedKeys)
		return con.prepareStatement(sql); 
    }

    public void moveResultSetAbsolutePosition(ResultSet result, int position)
    {
    	try
    	{
	    	if (androidCursorField == null)
	    	{
	    		this.androidCursorField = result.getClass().getDeclaredField("c");  // Es un atributo privado de SqldroidResultSet (no usar getField que requiere que sea público) 
	        	androidCursorField.setAccessible(true); // Es privado el atributo
	    	}    	
	    	
	    	Object cursor = androidCursorField.get(result); //android.database.Cursor    		    	
	    	
	    	if (androidMoveToPositionMethod == null)
	    	{
	    		// moveToPosition(int position)
	    		this.androidMoveToPositionMethod = cursor.getClass().getMethod("moveToPosition", new Class[]{int.class});  // Usamos getMethod porque es un método público de Android y no getDeclaredMethod no sea que no esté en esa clase exactamente
	    	}
	    	androidMoveToPositionMethod.invoke(cursor, position - 1);
		}
		catch (InvocationTargetException ex)
		{
			throw new JEPLException(ex);
		}          			
		catch (NoSuchMethodException ex)
		{
			throw new JEPLException(ex);
		}        			
		catch (IllegalAccessException ex)
		{
			throw new JEPLException(ex);
		}
		catch (NoSuchFieldException ex) 
		{
			throw new JEPLException(ex);
		}       	
    }
	    
}