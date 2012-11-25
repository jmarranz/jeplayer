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
public class Identifier extends Token
{
    protected String value = "";

    /** Creates a new instance of Identifier */
    public Identifier(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        parse(cursor);
    }

    public static boolean isIdentifierStart(char c)
    {
        return Character.isJavaIdentifierStart(c);
    }

    public static boolean isIdentifierPart(char c)
    {
        if (Character.isJavaIdentifierPart(c))
            return true;
        if (c == '-') // En HTML se admite como parte de identificadores
            return true;
        return false;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public void parse(Cursor cursor)
    {
        // cursor apunta al comienzo del identificador
        StringBuilder valueTmp = new StringBuilder();
        valueTmp.append( cursor.getCurrentChar() );
        cursor.inc(); // segunda letra (si hay)
        while(cursor.isValidPosition() &&
              isIdentifierPart(cursor.getCurrentChar()))
        {
            valueTmp.append( cursor.getCurrentChar() );
            cursor.inc();
        }

        this.value = valueTmp.toString();

        cursor.dec();
        this.end = cursor.getCurrentPos(); // apunta al �ltimo caracter del identificador
    }
}
