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

package jepl.impl.query;

import java.util.ArrayList;
import java.util.LinkedList;

import jepl.JEPLException;
import jepl.impl.lex.JDBCParamStandardToken;
import jepl.impl.lex.JDBCParamToken;
import jepl.impl.lex.JDBCParamWithNameToken;
import jepl.impl.lex.JDBCParamWithNumberToken;
import jepl.impl.lex.SourceCode;
import jepl.impl.lex.Token;
import jepl.impl.lex.TokenFilter;

/**
 *
 * @author jmarranz
 */
public class JEPLQueryParsedImpl
{
    protected static final TokenFilter tokenFilter = new TokenFilter()
    {
        @Override
        public boolean accept(Token token)
        {
            return token instanceof JDBCParamToken;
        }
    };
    protected String sqlOriginal; // Con los tipos de parámetros de JEPLayer
    protected String sqlJDBC; // Válido para JDBC
    protected ArrayList<JEPLParameterDecImpl> paramsByJDBCPosition;

    
    public JEPLQueryParsedImpl(String sqlOriginal)
    {
        this.sqlOriginal = sqlOriginal;

        if (isNeededParsing(sqlOriginal))
        {
            SourceCode sc = new SourceCode(sqlOriginal,tokenFilter);
            LinkedList<Token> tokenList = sc.getTokenList();
            StringBuilder sqlBuff = new StringBuilder();
            int currentSQLPos = 0;
            for(Token token : tokenList)
            {
                JDBCParamToken tokenParam = (JDBCParamToken)token;
                addParameter(tokenParam);

                int start = tokenParam.getStart();
                int end = tokenParam.getEnd();
                sqlBuff.append(sqlOriginal.substring(currentSQLPos, start));
                sqlBuff.append("?");
                currentSQLPos = end + 1;
            }

            sqlBuff.append(sqlOriginal.substring(currentSQLPos)); // El tramo último pendiente (o la SQL completa)

            this.sqlJDBC = sqlBuff.toString();
        }
        else
        {
            this.sqlJDBC = sqlOriginal;
        }
    }


    public String getSQLJDBC()
    {
        return sqlJDBC;
    }

    private void addParameter(JDBCParamToken tokenParam)
    {
        // Algunas ideas son de JPA
        // http://www.objectdb.com/java/jpa/query/parameter
        // Podemos mezclar tipos y repetir nombres:  "... ? ... ?2 ... :myname ... ?4 ... :myname ..."

        if (paramsByJDBCPosition == null)
            this.paramsByJDBCPosition = new ArrayList<JEPLParameterDecImpl>();

        int jdbcPosition = paramsByJDBCPosition.size() + 1;

        JEPLParameterDecImpl param = null;
        if (tokenParam instanceof JDBCParamStandardToken)
        {
            param = new JEPLParameterDecWithNumberImpl(jdbcPosition);
        }
        else if (tokenParam instanceof JDBCParamWithNumberToken)
        {
            int paramNumber = ((JDBCParamWithNumberToken)tokenParam).getNumber();
            // paramNumber DEBE ser igual a paramsByJDBCPosition.size() + 1  (ej. el primer param es 1 y size es 0 antes de insertar)

            if (paramNumber > jdbcPosition)
                throw new JEPLException("Parameter in SQL query with number ?" + paramNumber + " is bigger than expected ?" + jdbcPosition);
            else if (paramNumber < paramsByJDBCPosition.size())
                throw new JEPLException("Parameter in SQL query with number ?" + paramNumber + " is smaller than expected ?" + jdbcPosition);

            // paramNumber coincide con la posición JDBC
            param = new JEPLParameterDecWithNumberImpl(paramNumber);
        }
        else if(tokenParam instanceof JDBCParamWithNameToken)
        {
            String name = ((JDBCParamWithNameToken)tokenParam).getName();
            param = new JEPLParameterDecWithNameImpl(jdbcPosition,name);
        }
        // No hay más casos

        paramsByJDBCPosition.add(param);
    }


    public static boolean isNeededParsing(String sqlOriginal)
    {
        for(int i = 0; i < sqlOriginal.length(); i++)
        {
            char c = sqlOriginal.charAt(i);
            if (c == '?' || c == ':')
                return true; // Ojo que es posible que esté dentro de una cadena literal pero necesitamos parsear para saberlo
        }
        return false;
    }

    public ArrayList<JEPLParameterDecImpl> getParamsByJDBCPosition()
    {
        return paramsByJDBCPosition;
    }
}
