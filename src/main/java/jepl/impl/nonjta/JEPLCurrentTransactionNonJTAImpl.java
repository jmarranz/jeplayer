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
package jepl.impl.nonjta;

import java.sql.Connection;
import java.sql.SQLException;
import jepl.JEPLException;
import jepl.JEPLTransaction;
import jepl.impl.JEPLCurrentTransactionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLCurrentTransactionNonJTAImpl extends JEPLCurrentTransactionImpl implements JEPLTransaction
{
    protected boolean begin = false;

    public JEPLCurrentTransactionNonJTAImpl(JEPLNonJTAConnectionImpl conWrap)
    {
        super(conWrap);
    }

    @SuppressWarnings("unchecked")
    public <T> T getUnderlyingTransaction(Class<T> type)
    {
        return (T)conWrap.getConnection();
    }

    @Override
    public boolean isActive()
    {
        Connection con = conWrap.getConnection();
        try
        {
            return !con.getAutoCommit();
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
    }

    @Override
    public void begin()
    {
        super.begin();
        Connection con = conWrap.getConnection();
        try
        {
            con.setAutoCommit(false);
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
        this.begin = true;
    }

    @Override
    public void commit()
    {
        if (!begin) throw new JEPLException("begin() must be executed first");

        super.commit();
        
        Connection con = conWrap.getConnection();
        try
        {
            con.commit();
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
        finally
        {
            try
            {
                con.setAutoCommit(true);
            }
            catch (SQLException ex)
            {
                throw new JEPLException(ex);
            }
        }
    }

    @Override
    public void rollback()
    {
        if (!begin) throw new JEPLException("begin() must be executed first");
        
        super.rollback();
        Connection con = conWrap.getConnection();
        try
        {
            con.rollback();
        }
        catch(SQLException ex)
        {
            throw new JEPLException(ex);
        }
        finally
        {
            try
            {
                con.setAutoCommit(true);
            }
            catch (SQLException ex)
            {
                throw new JEPLException(ex);
            }
        }
    }
}
