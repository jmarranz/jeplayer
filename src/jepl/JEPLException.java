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

package jepl;

/**
 * The exception class used by the library sometimes to wrap SQLException exceptions.
 *
 * @author jmarranz
 */
@SuppressWarnings("serial")
public class JEPLException extends RuntimeException
{
    /**
     * Creates an exception with the specified message and cause.
     *
     * @param msg the error message of this exception.
     * @param cause the cause of this exception.
     */
    public JEPLException(String msg,Throwable cause)
    {
        super(msg,cause);
    }

    /**
     * Creates an exception with the specified message.
     *
     * @param msg the error message of this exception.
     */
    public JEPLException(String msg)
    {
        super(msg);
    }

    /**
     * Creates an exception with the specified cause.
     *
     * @param cause the cause of this exception.
     */
    public JEPLException(Throwable cause)
    {
        super(cause);
    }
}
