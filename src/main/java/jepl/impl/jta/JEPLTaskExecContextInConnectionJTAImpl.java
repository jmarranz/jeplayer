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

package jepl.impl.jta;

import jepl.JEPLTransactionPropagation;
import jepl.impl.JEPLTaskExecContextInConnectionImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLTaskExecContextInConnectionJTAImpl<T> extends JEPLTaskExecContextInConnectionImpl<T>
{
    protected JEPLTransactionPropagation txnPropInUse;

    public JEPLTaskExecContextInConnectionJTAImpl(JEPLTaskOneExecWithConnectionImpl<T> task,JEPLTransactionPropagation txnPropInUse)
    {
        super(task);
        this.txnPropInUse = txnPropInUse;
    }

    public JEPLTransactionPropagation getJEPLTransactionPropagation()
    {
        return txnPropInUse;
    }

    public JEPLJTAConnectionImpl getJEPLJTAConnection()
    {
        return (JEPLJTAConnectionImpl)getJEPLConnection();
    }
}

