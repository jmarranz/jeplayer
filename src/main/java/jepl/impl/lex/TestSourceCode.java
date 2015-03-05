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
public class TestSourceCode
{

    /** Creates a new instance of TestSourceCode */
    public TestSourceCode()
    {
    }

    public static void check(boolean value)
    {
        if (!value) throw new RuntimeException("TEST ERROR");
    }

    public static void main(String[] args)
    {
        String code;
        SourceCode srcCode;    

        code = "'hello ''guy'' '"; // En SQL es escapado de comillas son dos seguidas
        srcCode = SourceCode.newSourceCode(code);
        check(srcCode.tokenCount() == 1);
        check(code.equals(srcCode.toStringTokens()));

        code = " 'hello ''guy'' ' "; // En SQL es escapado de comillas son dos seguidas
        srcCode = SourceCode.newSourceCode(code);
        check(code.equals(srcCode.toStringTokens()));
        check(srcCode.tokenCount() == 3);
        srcCode = srcCode.trim();
        check(srcCode.tokenCount() == 1);

        code = " ? ";
        srcCode = SourceCode.newSourceCode(code);
        check(code.equals(srcCode.toStringTokens()));
        check(srcCode.tokenCount() == 3);
        srcCode = srcCode.trim();
        check(srcCode.tokenCount() == 1);
        check(srcCode.getToken(0) instanceof JDBCParamStandardToken);

        code = " ?12 ";
        srcCode = SourceCode.newSourceCode(code);
        check(code.equals(srcCode.toStringTokens()));
        check(srcCode.tokenCount() == 3);
        srcCode = srcCode.trim();
        check(srcCode.tokenCount() == 1);
        check(srcCode.getToken(0) instanceof JDBCParamWithNumberToken);
        check( ((JDBCParamWithNumberToken)srcCode.getToken(0)).getNumber() == 12 );
        
        code = " :myparam ";
        srcCode = SourceCode.newSourceCode(code);
        check(code.equals(srcCode.toStringTokens()));
        check(srcCode.tokenCount() == 3);
        srcCode = srcCode.trim();
        check(srcCode.tokenCount() == 1);
        check(srcCode.getToken(0) instanceof JDBCParamWithNameToken);


        code = "SELECT * WHERE CONTACT WHERE id = ? AND name = ' ? ?1 :notvalid ' AND phone = ?12 AND email = :email";
        srcCode = SourceCode.newSourceCode(code);
        int paramStd = 0, paramNumber = 0, paramName = 0;
        for(int i = 0; i < srcCode.tokenCount(); i++)
        {
            Token token = srcCode.getToken(i);
            if (token instanceof JDBCParamStandardToken)
            {
                paramStd++;
            }
            else if (token instanceof JDBCParamWithNumberToken)
            {
                paramNumber++;
                check( ((JDBCParamWithNumberToken)token).getNumber() == 12 );
            }
            else if (token instanceof JDBCParamWithNameToken)
            {
                paramName++;
                check( ((JDBCParamWithNameToken)token).getName().equals("email") );
            }
        }
        check(paramStd == 1);
        check(paramNumber == 1);
        check(paramName == 1);

    }

}
