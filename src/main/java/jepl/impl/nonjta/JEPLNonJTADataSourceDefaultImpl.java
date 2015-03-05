package jepl.impl.nonjta;

import javax.sql.DataSource;

public class JEPLNonJTADataSourceDefaultImpl extends JEPLNonJTADataSourceImpl
{
    public JEPLNonJTADataSourceDefaultImpl(JEPLBootNonJTAImpl boot,DataSource ds)
    {
        super(boot,ds);
    }
    
}
