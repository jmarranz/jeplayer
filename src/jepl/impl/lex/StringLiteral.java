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

import jepl.JEPLException;


/**
 *
 * @author jmarranz
 */
public abstract class StringLiteral extends Token
{
    protected String value = ""; // Contenido de la cadena sin comillas

    /**
     * Creates a new instance of StringLiteral
     */
    public StringLiteral(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        parse(cursor);
    }

    @Override
    public String toString()
    {
        char cf = getDelimiterChar();
        StringBuilder str = new StringBuilder();
        str.append(cf);
        for(int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            str.append(c);
            if (c == cf)
                str.append(cf); // Hay que añadir otra más  Ej: ''
        }
        str.append(cf);
        return str.toString();
    }

    public abstract char getDelimiterChar();

    public void parse(Cursor cursor)
    {
        // cursor apunta a la primera comilla
        // En SQL la comilla simple dentro de comillas simples se "escapa" poniendo dos seguidas
        StringBuilder valueTmp = new StringBuilder();
        cursor.inc(); // Siguiente a la primera comilla (contenido)
        char endChar = getDelimiterChar();
        while( cursor.isValidPosition() )
        {
            char c = cursor.getCurrentChar();
            if (endChar != c)
            {
                valueTmp.append( c );
                cursor.inc();
            }
            else
            {
                // Puede ser un finalizador o bien dos comillas seguidas => una comilla
                if (cursor.isLastPos())
                {
                    // No hay más caracteres por lo que es un finalizador
                    break;
                }
                else
                {
                    char csig = cursor.getNextChar();
                    if (endChar == csig) // Es también una comilla
                    {
                        valueTmp.append( c ); // Añadimos la comilla
                        cursor.inc(); // A la segunda comilla
                        cursor.inc(); // Avanzamos dos veces para pasar la segunda comilla
                    }
                    else
                    {
                        // No es una comilla por lo que es un finalizador
                        break;
                    }
                }
            }
        }

        if (!cursor.isValidPosition())
            throw new JEPLException("Missing matching " + endChar + " start pos: " + start + " code: \"" + cursor.getCode() + "\"");

        this.value = valueTmp.toString();

        this.end = cursor.getCurrentPos(); // apunta a la comilla finalizadora
    }
}
