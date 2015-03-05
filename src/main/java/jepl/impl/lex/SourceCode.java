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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author jmarranz
 */
public class SourceCode 
{
    protected LinkedList<Token> tokens;
    protected StringBuilder code;

    /** Creates a new instance of SourceCode */
    public SourceCode(String code,TokenFilter filter)
    {
        this.tokens = Token.parse(code,filter);
        this.code = new StringBuilder(code);
    }

    public SourceCode()
    {
        this.tokens = new LinkedList<Token>();
        this.code = new StringBuilder();
    }

    public SourceCode(LinkedList<Token> tokens)
    {
        this.tokens = tokens;
        this.code = new StringBuilder(toStringTokens(tokens));
    }

    public SourceCode(String code,LinkedList<Token> tokens)
    {
        this.code = new StringBuilder(code);
        this.tokens = tokens;
        // Se supone que code se corresponde con los tokens
    }

    public static SourceCode newSourceCode(String code)
    {
        return newSourceCode(code,null);
    }

    public static SourceCode newSourceCode(String code,TokenFilter filter)
    {
        return new SourceCode(code,filter);
    }

    @Override
    public boolean equals(Object other)
    {
        if (super.equals(other))
            return true;
        if (!(other instanceof SourceCode))
            return false;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    public void addToken(Token token)
    {
        tokens.add(token);
        code.append( token.toString() );
    }

    public int tokenCount()
    {
        return tokens.size();
    }

    public Token getToken(int i)
    {
        return tokens.get(i);
    }

    public LinkedList<Token> getTokenList()
    {
        return tokens;
    }
    
    @Override
    public String toString()
    {
        return code.toString();
    }

    public String toStringTokens()
    {
        return toStringTokens(tokens);
    }

    public static String toStringTokens(LinkedList<Token> tokens)
    {
        StringBuilder code = new StringBuilder();
        for(Iterator<Token> it = tokens.iterator(); it.hasNext(); )
        {
            Token token = it.next();
            code.append( token.toString() );
        }
        return code.toString();
    }

    public SourceCode[] split(Token byTok)
    {
        // Simula la funci�n String.split pero con tokens
        SourceCode[] resTmp = new SourceCode[tokens.size() / 2 + 1]; // En este array caben todas las posibles soluciones
        SourceCode current = new SourceCode();
        resTmp[0] = current;
        int i = 0;
        for(Iterator<Token> it = tokens.iterator(); it.hasNext(); )
        {
            Token token = it.next();
            if (token.equals(byTok))
            {
                if (current.tokenCount() > 0) // Si no se cumple es que el actual est� vac�o, lo ignoramos y perdemos
                    i++;
                current = new SourceCode();
                resTmp[i] = current;
            }
            else
            {
                current.addToken(token);
            }
        }

        if (current.tokenCount() == 0)
            i--; // los vac�os no se incluyen, podr�a ser -1

        SourceCode[] res = new SourceCode[i + 1];
        for(int j = 0; j <= i; j++)
            res[j] = resTmp[j];
        return res;
    }

    public SourceCode trim()
    {
        LinkedList<Token> resTokens = new LinkedList<Token>();
        resTokens.addAll(this.tokens);

        boolean modified = false;

        for(ListIterator<Token> it = resTokens.listIterator(); it.hasNext(); )
        {
            Token token = it.next();
            if (!token.getClass().equals(Space.class))
                break;
            it.remove();
            modified = true;
        }

        for(ListIterator<Token> it = resTokens.listIterator(resTokens.size()); it.hasPrevious(); )
        {
            Token token = it.previous();
            if (!token.getClass().equals(Space.class))
                break;
            it.remove();
            modified = true;
        }

        if (modified)
            return new SourceCode(resTokens);
        else
            return new SourceCode(code.toString(),resTokens);
    }
}
