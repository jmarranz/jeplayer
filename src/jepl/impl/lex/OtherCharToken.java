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
public class OtherCharToken extends Token
{
    protected final static OtherCharToken SINGLETON = new OtherCharToken();
    protected char ch;
    
    /** Creates a new instance of Comma */
    public OtherCharToken(char ch,int start)
    {
        super(start);
        this.ch = ch;
        this.end = start;
    }

    public OtherCharToken()
    {
    }

    @Override
    public String toString()
    {
        return Character.toString(ch);
    }

    public static OtherCharToken getSingleton()
    {
        return SINGLETON;
    }
}
