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

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 *
 * @author jmarranz
 */
public class JEPLResultSetDAOIteratorImpl<T> implements ListIterator<T>
{
    protected JEPLResultSetDAOImpl<T> resultSet;
    protected boolean hasNext = false;
    protected boolean knowHasNext = false;

    public JEPLResultSetDAOIteratorImpl(JEPLResultSetDAOImpl<T> resultSet)
    {
        this.resultSet = resultSet;
    }

    public JEPLResultSetDAOIteratorImpl(JEPLResultSetDAOImpl<T> resultSet,int index)
    {
        this.resultSet = resultSet;
        for(int i = 0; i < index; i++) // Llamamos a next() tantas veces como pone index
            next();
    }    

    public boolean hasNext()
    {
        if (knowHasNext) return hasNext;

        this.hasNext = resultSet.next();
        this.knowHasNext = true;
        return hasNext;
    }

    public T next()
    {
        if (knowHasNext)
        {
             if (!hasNext) throw new NoSuchElementException();
        }
        else
        {
            if (!hasNext()) throw new NoSuchElementException();
        }
        T res = resultSet.getObject();
        this.knowHasNext = false;
        return res;
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPrevious()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T previous()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int nextIndex()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int previousIndex()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void set(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(T e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
