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

import java.sql.SQLException;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLTaskExecContextInConnectionImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLTaskExecContextInConnectionNonJTAImpl<T> extends JEPLTaskExecContextInConnectionImpl<T>
{
    protected boolean previousCommitMode;

    public JEPLTaskExecContextInConnectionNonJTAImpl(JEPLTaskOneExecWithConnectionImpl<T> task)
    {
        super(task);
    }

    public JEPLNonJTAConnectionImpl getJEPLNonJTAConnection()
    {    
        return (JEPLNonJTAConnectionImpl)getJEPLConnection();
    }

    @Override
    public void setJEPLConnection(JEPLConnectionImpl jcon) throws SQLException
    {
        if (jcon != null)
        {
            // Memorizamos el estado actual pues puede cambiar en el caso de tasks anidadas
            this.previousCommitMode = jcon.getConnection().getAutoCommit();
        }
        else
        {
            // Esta task terminó, en el caso de tasks anidadas es preciso restaurar
            // el valor del autoCommit de antes de ejecutarse esta task
            getJEPLNonJTAConnection().configureAutoCommit(previousCommitMode);
        }

        super.setJEPLConnection(jcon);
    }
}

