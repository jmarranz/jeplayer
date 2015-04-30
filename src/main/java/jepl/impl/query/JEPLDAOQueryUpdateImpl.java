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
import java.util.Map;
import jepl.JEPLColumnDesc;
import jepl.JEPLException;
import jepl.JEPLPersistAction;
import jepl.JEPLUpdateDAOListener;
import jepl.impl.JEPLConnectionImpl;
import jepl.impl.JEPLDAOImpl;
import jepl.impl.JEPLTaskOneExecWithConnectionImpl;

/**
 *
 * @author jmarranz
 * @param <T>
 */
public class JEPLDAOQueryUpdateImpl<T> extends JEPLDAOQueryImpl<T>
{   
    protected T obj;
    protected JEPLPersistAction action;
    protected boolean prepared;
    
    public JEPLDAOQueryUpdateImpl(JEPLDAOImpl<T> dal,T obj,JEPLPersistAction action)
    {
        super(dal,null);
        
        this.obj = obj;
        this.action = action;
        this.prepared = false;
        
        // No parseamos ahora: parseSQL();        
    }

    @Override
    public void parseSQL()
    {
        if (prepared) super.parseSQL();
    }
    
    @Override
    public int executeUpdate(JEPLConnectionImpl jcon) throws Exception
    {
        prepareBeforeExecuting(jcon);
                
        return super.executeUpdate(jcon);
    }    
    
    @Override    
    protected <U> U getGeneratedKey(JEPLConnectionImpl jcon,final Class<U> returnType) throws Exception
    {    
        prepareBeforeExecuting(jcon);   
        return super.getGeneratedKey(jcon,returnType);        
    }
      
    @Override    
    protected <U> U getOneRowFromSingleField(JEPLConnectionImpl jcon,final Class<U> returnType) throws Exception      
    {
        prepareBeforeExecuting(jcon);   
        return super.getOneRowFromSingleField(jcon,returnType);          
    }
        
    @Override
    public String getCode()
    {
        if (prepared) return super.getCode();
                    
        try
        {
            JEPLConnectionImpl conWrap = getJEPLDataSourceImpl().getCurrentJEPLConnectionImpl();
            if (conWrap == null)
            {
                JEPLTaskOneExecWithConnectionImpl<String> task = new JEPLTaskOneExecWithConnectionImpl<String>()
                {
                    @Override
                    public String execInherit() throws Exception
                    {
                        return getCode(getJEPLConnection());
                    }
                };
                return execWithTask(task);
            }
            else
            {
                return getCode(conWrap);
            }
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
    
    protected String getCode(JEPLConnectionImpl jcon) throws Exception
    {
        prepareBeforeExecuting(jcon);
        return super.getCode();
    }
            
    private void prepareBeforeExecuting(JEPLConnectionImpl jcon) throws Exception
    {
        if (prepared) return;
        
        final JEPLUpdateDAOListener<T> listener = getJEPLUpdateDAOListener();
        
        String tableName = listener.getTable(jcon, obj);
            
        Map.Entry<JEPLColumnDesc,Object>[] columnValueList = listener.getColumnDescAndValues(jcon, obj, action);        
        ArrayList<Object> paramValueList = new ArrayList<Object>();
               
        String sql;
        switch(action)
        {
            case INSERT:
            {
                StringBuilder sqlColumns = new StringBuilder();                
                StringBuilder sqlVariables = new StringBuilder();              
                boolean doneFirst = false;
                for(Map.Entry<JEPLColumnDesc,Object> colValue : columnValueList)
                {
                    JEPLColumnDesc colDesc = colValue.getKey();
                    if (colDesc.isAutoIncrement()) continue; // Excluimos las columnas que se autoincrementan pues lo normal es que sean también primary keys (y por tanto generada por la BD)
                    if (doneFirst) 
                    {
                        sqlColumns.append(',');
                        sqlVariables.append(',');
                    }
                    sqlColumns.append(colDesc.getName());
                    sqlVariables.append('?');
                    paramValueList.add(colValue.getValue());

                    if (!doneFirst) doneFirst = true;
                }                
                sql = "INSERT INTO " + tableName + " (" + sqlColumns + ") VALUES (" + sqlVariables + ")"; 
                break;
            }
            case UPDATE:   
            {
                StringBuilder sqlColumns = new StringBuilder();                               
                boolean doneFirst = false;
                for(Map.Entry<JEPLColumnDesc,Object> colValue : columnValueList)
                {
                    JEPLColumnDesc colDesc = colValue.getKey();
                    if (colDesc.isPrimaryKey() || colDesc.isImportedKey()) continue;
                    if (doneFirst) 
                    {
                        sqlColumns.append(',');
                    }
                    sqlColumns.append(colDesc.getName() + " = ?"); 
                    paramValueList.add(colValue.getValue());
                    
                    if (!doneFirst) doneFirst = true;
                }     
                
                StringBuilder sqlKeys = new StringBuilder();                
                doneFirst = false;
                for(Map.Entry<JEPLColumnDesc,Object> colValue : columnValueList)
                {
                    JEPLColumnDesc colDesc = colValue.getKey();
                    if (!colDesc.isPrimaryKey() && !colDesc.isImportedKey()) continue;
                    if (doneFirst) 
                    {
                        sqlKeys.append(" AND ");
                    }
                    sqlKeys.append(colDesc.getName() + " = ?");
                    paramValueList.add(colValue.getValue());
                    
                    if (!doneFirst) doneFirst = true;
                }                     
                
                sql = "UPDATE " + tableName + " SET " + sqlColumns + " WHERE " + sqlKeys; 
                break;                
            }
            case DELETE:
            {
                StringBuilder sqlKeys = new StringBuilder();                
                boolean doneFirst = false;
                for(Map.Entry<JEPLColumnDesc,Object> colValue : columnValueList)
                {
                    JEPLColumnDesc colDesc = colValue.getKey();
                    if (!colDesc.isPrimaryKey() && !colDesc.isImportedKey()) continue;
                    if (doneFirst) 
                    {
                        sqlKeys.append(" AND ");
                    }
                    sqlKeys.append(colDesc.getName() + " = ?");
                    paramValueList.add(colValue.getValue());
                    
                    if (!doneFirst) doneFirst = true;
                }                     
                
                sql = "DELETE FROM " + tableName + " WHERE " + sqlKeys; 
                break;                
            }   
            
            default: throw new JEPLException("Internal Error");
        }
        
        
        this.sqlOriginal = sql;
        
        this.prepared = true;
        
        parseSQL(); // Ahora si ya se parsea el SQL para poder usar los parámetros
        
        for(Object paramValue : paramValueList)
        {
            addParameter(paramValue);
        }
    }        
    
    private JEPLUpdateDAOListener<T> getJEPLUpdateDAOListener()
    {
        // El retorno no puede ser nulo, necesitamos un listener para saber como
        // generar el insert, update etc en el caso de usar los métodosDAO  resumidos

        JEPLUpdateDAOListener<T> listener;
        if (listenerList != null)
        {
            listener = listenerList.getJEPLUpdateDAOListener();
            if (listener != null)
                return listener;
        }

        listener = getJEPLDAOImpl().getJEPLListenerList().getJEPLUpdateDAOListener();
        if (listener != null)
            return listener;

        //listener = getJEPLDataSourceImpl().getJEPLListenerList().getJEPLUpdateDAOListener();
        // Es necesario que haya uno
        throw new JEPLException("Missing listener implementing " + JEPLUpdateDAOListener.class + " registered on DAO or query/update object");        
    }        
    
    
}
