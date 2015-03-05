package jepl.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JEPLPreparedStatementDefaultImpl extends JEPLPreparedStatementImpl 
{
	public JEPLPreparedStatementDefaultImpl(JEPLConnectionImpl jcon,JEPLDALImpl dal,PreparedStatement stmt,String key) throws SQLException
	{
		super(jcon,dal,stmt,key);
	}
	
}
