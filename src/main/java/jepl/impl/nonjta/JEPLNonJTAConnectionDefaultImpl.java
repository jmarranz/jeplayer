package jepl.impl.nonjta;

import java.sql.Connection;

public class JEPLNonJTAConnectionDefaultImpl extends JEPLNonJTAConnectionImpl 
{
    public JEPLNonJTAConnectionDefaultImpl(JEPLNonJTADataSourceImpl ds,Connection con)
    {
        super(ds,con);
    }
}
