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
 * Espacio en sentido amplio (espacio, final de l�nea etc)
 * @author jmarranz
 */
public class Space extends Token
{
    protected char c;

    /** Creates a new instance of Space */
    public Space(char c,int start)
    {
        super(start);
        this.c = c;
        this.end = start;
    }

    public Space(char c)
    {
        this(c,0);
    }

    public static boolean isSpace(char c)
    {
        return Character.isSpaceChar(c);
    }

    @Override
    public String toString()
    {
        return Character.toString(c);
    }
}
