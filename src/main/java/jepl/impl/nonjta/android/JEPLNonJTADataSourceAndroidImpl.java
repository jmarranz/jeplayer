package jepl.impl.nonjta.android;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import jepl.impl.JEPLConnectionImpl;
import jepl.impl.nonjta.JEPLBootNonJTAImpl;
import jepl.impl.nonjta.JEPLNonJTADataSourceImpl;

public class JEPLNonJTADataSourceAndroidImpl extends JEPLNonJTADataSourceImpl
{
	protected AtomicBoolean sqlDroidChecked = new AtomicBoolean(false);
	protected volatile boolean sqlDroid = false;
	
    public JEPLNonJTADataSourceAndroidImpl(JEPLBootNonJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
    }
    
    @Override
    public JEPLConnectionImpl createJEPLConnection(Connection con) throws SQLException
    {
    	if (!sqlDroidChecked.get())
    	{
    		String driverNameLower = con.getMetaData().getDriverName().toLowerCase();
    		this.sqlDroid = driverNameLower.contains("sqldroid");
    		sqlDroidChecked.set(true);
    	}
    	
    	if (sqlDroid)
    		return new JEPLNonJTAConnectionSQLDroidImpl(this,con); // Este constructor no dará nunca error    	
    	else 
    		return super.createJEPLConnection(con);
    }    
}
