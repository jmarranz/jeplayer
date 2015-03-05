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
public class JDBCParamStandardToken extends JDBCParamToken
{

    /**
     * Creates a new instance of FloatNumber
     */
    public JDBCParamStandardToken(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        this.end = cursor.getCurrentPos();
    }

    public static boolean isJDBCParamStandardToken(char c,Cursor cursor)
    {
        if (c != '?') return false;
        // Vemos si el siguiente caracter NO es el comienzo de un número
        // (sería el caso de ?1 que es válido pero no es este token) pues lo que
        // se espera después es un espacio, un tabulador, un fin de línea
        // una coma, un paréntesis (por el caso VALUES(?,?) ) etc. Si siguiera el comienzo de un identificador
        // (?algo) no lo consideramos como un JDBCParamStandardToken y seguramente dará error de sintaxis SQL
        // pues yo creo que JDBC exige una separación.
        // Descartamos que siga un número entero para descartar el caso de params ?1 ?2 etc

        if (cursor.isLastPos()) return true; // Es un ? al final

        char c2 = cursor.getNextChar();
        if (Identifier.isIdentifierStart(c2)) return false;
        if (Character.isDigit(c2)) return false; // Es el caso de ?1 etc que es diferente

        return true;
    }

    @Override
    public String toString()
    {
        return "?";
    }

}
