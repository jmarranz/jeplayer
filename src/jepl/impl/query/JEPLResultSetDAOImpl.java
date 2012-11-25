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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import jepl.JEPLException;
import jepl.JEPLResultSetDAO;
import jepl.JEPLResultSetDAOListener;
import jepl.impl.JEPLPreparedStatementImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLResultSetDAOImpl<T> extends JEPLResultSetImpl implements JEPLResultSetDAO<T>
{
    protected JEPLResultSetDAOListener<T> listener;
    protected LinkedList<T> internalList = new LinkedList<T>();
    protected boolean constructedObject; // NO INICIAR A FALSE NI A TRUE
    protected boolean detectedDebugMode;  // NO INICIAR A FALSE NI A TRUE

    public JEPLResultSetDAOImpl(JEPLDAOQueryImpl<T> query,JEPLPreparedStatementImpl stmt,ResultSet result,JEPLResultSetDAOListener<T> listener) throws SQLException
    {
        super(query,stmt,result);
        this.listener = listener;
        this.constructedObject = true;
    }

    @SuppressWarnings("unchecked")
    public JEPLDAOQueryImpl<T> getJEPLDAOQueryImpl()
    {
        return (JEPLDAOQueryImpl<T>)query;
    }

    public LinkedList<T> getInternalList()
    {
        return internalList;
    }

    public boolean next()
    {
        if (isClosed()) throw new JEPLException("This result set is already closed, use normal List methods instead");
        boolean res;
        try
        {
            res = result.next();
        }
        catch (SQLException ex) { throw new JEPLException(ex); }

        if (!res)
        {
            close(); // Cerramos automáticamente al terminar de iterar para que actue la colección interna desde ahora

            query.checkNumOfReturnedRows( count() ); // Por intentar usar la regla del usuario de limitación de filas SIEMPRE (aunque tenga poco valor la verdad)
        }
        return res;
    }

    public T getObject()
    {
        if (isClosed()) throw new JEPLException("This result set is already closed, use normal List methods instead");

        try
        {
            T obj = listener.createObject(this);
            if (obj != null) // El ser null es como un "continue", útil para filtrar objetos            
                listener.fillObject(obj,this);

            // En el caso de obj == null añadimos el null a la List interna pues si utilizáramos el List
            // después esperamos encontrarnos con lo mismo que hemos iterado
            internalList.add(obj);
            return obj;
        }
        catch (JEPLException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new JEPLException(ex);
        }
    }

    public boolean isStopped()
    {
        throw new JEPLException("This call has no sense in this context");
    }

    public void stop()
    {
        throw new JEPLException("This call has no sense in this context");
    }

    public void checkResultSetNotClosed()
    {
        if (!isClosed()) throw new JEPLException("This method cannot be called when ResultSet is still open");
    }


    public void fetchToIndex(int index) // Método interno
    {
        if (isClosed()) return;

        int size = internalList.size();
        int diff = size - 1 - index;
        if (diff >= 0) return;
        else
        {
            for(int i = 0; i < (-diff); i++)
            {
                next();
                getObject();
            }
        }
    }

    public void fetchToTheEndIfNotClosed() // Método interno
    {
//checkResultSetNotClosed();

        if (isClosed()) return; // No hace falta
        while(next())
        {
            getObject();
        }
        // A la salida de este método el ResultSet estará cerrado
    }

    public int count()
    {
        fetchToTheEndIfNotClosed();
        return internalList.size();
    }

    // Desde aquí métodos implementación de List
    public int size()
    {
        if (!constructedObject)
        {
            // Se ha llamado a size() antes de que se construta el objeto, esto es cosa del modo
            // debug en NetBeans que en el caso de un List muestra el size() de la colección
            // en el campo "Value of Variable"
            // Hay que tener en cuenta que pasamos por aquí antes de pasar por el constructor
            // y de definir los valores iniciales de los atributos (si hubiera que NO deben tener para no sobreescribir detectedDebugMode)
            this.detectedDebugMode = true;
        }

        if (detectedDebugMode && !isClosed())
        {
            JEPLException ex = new JEPLException("size() method cannot be called in debug mode and ResultSet not closed");
            ex.printStackTrace();
            throw ex;
        }

        return count();
    }

    public boolean isEmpty()
    {
        fetchToTheEndIfNotClosed();
        return internalList.isEmpty();
    }

    public boolean contains(Object o)
    {
        fetchToTheEndIfNotClosed();
        return internalList.contains(o);
    }

    public Iterator<T> iterator()
    {
        if (isClosed()) return internalList.iterator();
        else return new JEPLResultSetDAOIteratorImpl<T>(this);
    }

    public Object[] toArray()
    {
        fetchToTheEndIfNotClosed();
        return internalList.toArray();
    }

    public <U> U[] toArray(U[] a)
    {
        fetchToTheEndIfNotClosed();
        return internalList.toArray(a);
    }

    public boolean add(T e)
    {
        fetchToTheEndIfNotClosed();
        return internalList.add(e);
    }

    public boolean remove(Object o)
    {
        fetchToTheEndIfNotClosed();
        return internalList.remove(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        fetchToTheEndIfNotClosed();
        return internalList.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c)
    {
        fetchToTheEndIfNotClosed();
        return internalList.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c)
    {
        fetchToTheEndIfNotClosed();
        return internalList.addAll(index,c);
    }

    public boolean removeAll(Collection<?> c)
    {
        fetchToTheEndIfNotClosed();
        return internalList.removeAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        fetchToTheEndIfNotClosed();
        return internalList.retainAll(c);
    }

    public void clear()
    {
        fetchToTheEndIfNotClosed();
        internalList.clear();
    }

    
    public T get(int index)
    {
        fetchToIndex(index);
        return internalList.get(index);
    }

    public T set(int index, T element)
    {
        fetchToIndex(index);
        return internalList.set(index,element);
    }

    public void add(int index, T element)
    {
        fetchToTheEndIfNotClosed();
        internalList.add(index,element);
    }

    public T remove(int index)
    {
        fetchToTheEndIfNotClosed();
        return internalList.remove(index);
    }

    public int indexOf(Object o)
    {
        fetchToTheEndIfNotClosed();
        return internalList.indexOf(o);
    }

    public int lastIndexOf(Object o)
    {
        fetchToTheEndIfNotClosed();
        return internalList.lastIndexOf(o);
    }

    public ListIterator<T> listIterator()
    {
        if (isClosed()) return internalList.listIterator();
        else return new JEPLResultSetDAOIteratorImpl<T>(this);
    }

    public ListIterator<T> listIterator(int index)
    {
        if (isClosed()) return internalList.listIterator();
        else return new JEPLResultSetDAOIteratorImpl<T>(this,index);
    }

    public List<T> subList(int fromIndex, int toIndex)
    {
        fetchToIndex(fromIndex);
        fetchToIndex(toIndex);
        return internalList.subList(fromIndex,toIndex);
    }
}
