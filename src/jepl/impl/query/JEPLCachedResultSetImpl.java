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
import java.util.List;
import jepl.JEPLException;
import jepl.JEPLCachedResultSet;
import jepl.impl.JEPLUtilImpl;

/**
 *
 * @author jmarranz
 */
public class JEPLCachedResultSetImpl implements JEPLCachedResultSet
{
    protected String[] colLabels;
    protected ArrayList<Object[]> values;

    public JEPLCachedResultSetImpl(String[] colLabels,ArrayList<Object[]> values)
    {
        this.colLabels = colLabels;
        this.values = values;
    }

    public String[] getColumnLabels()
    {
        return colLabels;
    }

    public List<Object[]> getValues()
    {
        return values;
    }

    public Object[] getRowArray(int row)
    {
        return values.get(row - 1);  // row empieza en 1
    }

    public Object getObjectValue(int row,int columnIndex)
    {
        return getRowArray(row)[columnIndex - 1];  // columnIndex empieza en 1
    }

    public int getColumIndex(String columnLabel)
    {
        for(int i = 0; i < colLabels.length; i++)
        {
            if (colLabels[i].equalsIgnoreCase(columnLabel))
                return i + 1; // Empieza en 1
        }
        throw new JEPLException("Column label not found:\"" + columnLabel + "\"");
    }

    public <T> T getValueCast(Object obj,Class<T> returnType)
    {
        return JEPLUtilImpl.cast(obj, returnType);
    }

    public int getColumnCount()
    {
        return colLabels.length;
    }

    public String getColumnLabel(int columnIndex)
    {
        return colLabels[columnIndex - 1];
    }
    
    public int size()
    {
        return values.size();
    }

    public <T> T getValue(int row, int columnIndex, Class<T> type)
    {
        Object obj = getObjectValue(row,columnIndex);
        return getValueCast(obj,type);
    }

    public <T> T getValue(int row, String columnLabel, Class<T> type)
    {
        return getValue(row,getColumIndex(columnLabel),type);
    }

    public Object getObject(int row, int columnIndex)
    {
        return getObjectValue(row,columnIndex);
    }

    public Object getObject(int row, String columnLabel)
    {
        return getObject(row,getColumIndex(columnLabel));
    }
}
