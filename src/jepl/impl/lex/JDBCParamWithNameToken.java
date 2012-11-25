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
public class JDBCParamWithNameToken extends JDBCParamToken
{
    protected Identifier identifier;

    /**
     * Creates a new instance of FloatNumber
     */
    public JDBCParamWithNameToken(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        parse(cursor);
    }

    public static boolean isJDBCParamWithNameToken(char c,Cursor cursor)
    {
        if (c != ':') return false;
        // Vemos si el siguiente caracter es el comienzo de un identificador
        if (cursor.isLastPos()) return false; // No sigue un identificador

        char c2 = cursor.getNextChar();
        if (!Identifier.isIdentifierStart(c2)) return false;

        return true;
    }

    @Override
    public String toString()
    {
        return ":" + identifier.toString();
    }

    public String getName()
    {
        return identifier.toString();
    }

    public void parse(Cursor cursor)
    {
        StringBuilder valueTmp = new StringBuilder();
        valueTmp.append( cursor.getCurrentChar() ); // El :
        cursor.inc(); // Comienzo del identificador
        this.identifier = new Identifier(cursor);

        this.end = identifier.getEnd(); // apunta al último caracter del identificador
    }
}
