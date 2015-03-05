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
package jepl.impl.lex;

import jepl.JEPLException;

/**
 *
 * @author jmarranz
 */
public class Cursor
{   
    protected int pos = 0;
    protected String code;

    /** Creates a new instance of Cursor */
    public Cursor(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public int getLength()
    {
        return code.length();
    }

    public boolean isFirstPos()
    {
        return pos == 0;
    }

    public boolean isLastPos()
    {
        return pos == (getLength() - 1);
    }

    public boolean isInTheEnd()
    {
        return pos == getLength();
    }

    public boolean isValidPosition()
    {
        if (isInTheEnd()) return false;
        if (getLength() == 0) return false;
        return true;
    }

    public boolean hasNext()
    {
        return (pos + 1) <= (getLength() - 1);
    }
    
    public char getCurrentChar()
    {
        int i = getCurrentPos();
        return code.charAt(i);
    }

    public char getNextChar()
    {
        int i = getCurrentPos();
        return code.charAt(i + 1);
    }

    public int getCurrentPos()
    {
        if (!isValidPosition())
        {
            if (getLength() == 0) throw new JEPLException("INTERNAL ERROR: code is empty");
            if (isInTheEnd()) throw new JEPLException("INTERNAL ERROR: cursor in the end");
        }
        return pos;
    }

    public int inc()
    {
        // Podemos ponernos en la posición siguiente a la última pero no más allá, así evitamos bucles infinitos absurdos
        if (isInTheEnd()) throw new JEPLException("INTERNAL ERROR: cursor is already in the end");
        pos++;
        return pos;
    }

    public int dec()
    {
        if (isFirstPos()) throw new JEPLException("INTERNAL ERROR: cursor is already in first position");
        pos--;
        return pos;
    }
}
