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

import java.util.LinkedList;

/**
 * Una vez creado es de s�lo lectura
 *
 * @author jmarranz
 */
public abstract class Token
{
    protected int start;
    protected int end;

    /** Creates a new instance of Token */
    public Token(int start)
    {
        this.start = start;
    }

    public Token()
    {
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    public static LinkedList<Token> parse(String code,TokenFilter filter)
    {
        Cursor cursor = new Cursor(code);
        return parse(cursor,false,' ',filter);
    }

    public static LinkedList<Token> parse(Cursor cursor,boolean checkEndChar,char endChar,TokenFilter filter)
    {
        LinkedList<Token> tokens = new LinkedList<Token>();
        for( ; cursor.isValidPosition(); cursor.inc())
        {
            int i = cursor.getCurrentPos();
            char c = cursor.getCurrentChar();
            if (checkEndChar && (c == endChar))
                break;

            Token token;
            if (Space.isSpace(c)) // Detectamos los espacios por aquello de conservar el método trim()
            {
                token = new Space(c,i);
            }
            else if (c == '\'')
            {
                token = new StringSimpleQuote(cursor);
            }
            else if (JDBCParamStandardToken.isJDBCParamStandardToken(c,cursor)) // Caso de ?  solitario
            {
                token = new JDBCParamStandardToken(cursor);
            }
            else if (JDBCParamWithNumberToken.isJDBCParamWithNumberToken(c,cursor)) // Caso de ?1 etc
            {
                token = new JDBCParamWithNumberToken(cursor);
            }
            else if (JDBCParamWithNameToken.isJDBCParamWithNameToken(c,cursor)) // Caso de ?1 etc
            {
                token = new JDBCParamWithNameToken(cursor);
            }
            else if (Identifier.isIdentifierStart(c))
            {
                // Identificamos este token con la única finalidad de disminuir el número
                // de tokens (objetos) final tras el parseo
                token = new Identifier(cursor);
            }
            else
            {
                token = new OtherCharToken(c,i);
            }
            // else throw new JEPLException("Unexpected char, pos: " + cursor.getCurrentPos() + " code: " + code);

            boolean accept = true;
            if (filter != null) accept = filter.accept(token);
                
            if (accept)
                tokens.add(token);
        }
        return tokens;
    }

    @Override
    public boolean equals(Object token)
    {
        if (super.equals(token))
            return true; // identidad de objetos
        if (!getClass().equals(token.getClass()))
            return false; // No pueden ser iguales si son de diferente clase
        return toString().equals(token.toString()); // Mismo tipo y mismo contenido
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
}
