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

/**
 *
 * @author jmarranz
 */
public class JDBCParamWithNumberToken extends JDBCParamToken
{
    protected String value = "";
    protected int number;

    /**
     * Creates a new instance of FloatNumber
     */
    public JDBCParamWithNumberToken(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        parse(cursor);
    }

    public static boolean isJDBCParamWithNumberToken(char c,Cursor cursor)
    {
        if (JDBCParamStandardToken.isJDBCParamStandardToken(c, cursor))
            return false;
        
        if (c != '?') return false;
        // Vemos si el siguiente caracter es el comienzo de un número
        if (cursor.isLastPos()) return false; // No sigue un número

        char c2 = cursor.getNextChar();
        if (!Character.isDigit(c2)) return false;

        return true;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public int getNumber()
    {
        return number;
    }
    
    public void parse(Cursor cursor)
    {
        StringBuilder valueTmp = new StringBuilder();
        valueTmp.append( cursor.getCurrentChar() ); // El ?
        cursor.inc(); // Comienzo del número
        while(cursor.isValidPosition() &&
              Character.isDigit(cursor.getCurrentChar()))
        {
            valueTmp.append( cursor.getCurrentChar() );
            cursor.inc();
        }

        this.value = valueTmp.toString();

        this.number = Integer.parseInt(valueTmp.substring(1));

        cursor.dec();
        this.end = cursor.getCurrentPos(); // apunta al último caracter del número
    }
}
