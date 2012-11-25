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

package jepl.impl;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import jepl.JEPLException;

/**
 *
 * @author jmarranz
 */
public class JEPLUtilImpl
{

    public static <T> T castDynamic(Object obj,Class<T> returnType)
    {
        try
        {
            Constructor<T> constr = returnType.getConstructor(obj.getClass());
            return constr.newInstance(obj);
        }
        catch (Exception ex)
        {
            throw new JEPLException("Cannot cast " + obj.getClass() + " to " + returnType,ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj,Class<T> returnType)
    {
        // Nuestros casts serán sencillos no como en:
        // http://www.docjar.com/html/api/com/mysql/jdbc/ResultSetImpl.java.html
        // No tiene sentido por ejemplo convertir una cadena conteniendo un valor real
        // a double y luego convertir a integer por ejemplo, es absurdo guardar
        // un valor real como cadena y luego pedirlo como entero via APIs de persistencia
        // lo lógico es hacer esa conversión fuera de la persistencia.

        if (obj == null) return null;

        if (returnType.isAssignableFrom(obj.getClass()))
            return (T)obj;

        if (returnType.equals(String.class))
            return (T)obj.toString();

        if (obj instanceof Number)
        {
            if (returnType.equals(Byte.class) || returnType.equals(byte.class))
                return (T)(Byte)((Number)obj).byteValue();
            else if (returnType.equals(Short.class) || returnType.equals(short.class))
                return (T)(Short)((Number)obj).shortValue();
            else if (returnType.equals(Integer.class) || returnType.equals(int.class))
                return (T)(Integer)((Number)obj).intValue();
            else if (returnType.equals(Long.class) || returnType.equals(long.class))
                return (T)(Long)((Number)obj).longValue();
            else if (returnType.equals(Float.class) || returnType.equals(float.class))
                return (T)(Float)((Number)obj).floatValue();
            else if (returnType.equals(Double.class) || returnType.equals(double.class))
                return (T)(Double)((Number)obj).doubleValue();
            else if (returnType.equals(BigInteger.class))
            {
                long value = cast(obj,long.class);
                return (T)new BigInteger(Long.toString(value));
            }
            else if (returnType.equals(BigDecimal.class))
            {
                double value = cast(obj,double.class);
                return (T)new BigDecimal(value);
            }
            else if (returnType.equals(java.sql.Date.class))
            {
                long value = cast(obj,long.class);
                return (T)new java.sql.Date(value);
            }
            else if (returnType.equals(java.sql.Time.class))
            {
                long value = cast(obj,long.class);
                return (T)new java.sql.Time(value);
            }
            else if (returnType.equals(java.sql.Timestamp.class))
            {
                long value = cast(obj,long.class);
                return (T)new java.sql.Time(value);
            }

            return castDynamic(obj,returnType);
        }
        else if (obj instanceof String)
        {
            if (returnType.equals(Boolean.class) || returnType.equals(Boolean.class))
                return (T)Boolean.valueOf((String)obj);
            if (returnType.equals(Byte.class) || returnType.equals(byte.class))
                return (T)new Byte((String)obj);
            else if (returnType.equals(Character.class) || returnType.equals(char.class))
                return (T)Character.valueOf( ((String)obj).charAt(0) );
            else if (returnType.equals(Short.class) || returnType.equals(short.class))
                return (T)Short.valueOf((String)obj);
            else if (returnType.equals(Integer.class) || returnType.equals(int.class))
                return (T)Integer.valueOf((String)obj);
            else if (returnType.equals(Long.class) || returnType.equals(long.class))
                return (T)Long.valueOf((String)obj);
            else if (returnType.equals(Float.class) || returnType.equals(float.class))
                return (T)Float.valueOf((String)obj);
            else if (returnType.equals(Double.class) || returnType.equals(double.class))
                return (T)Double.valueOf((String)obj);
            else if (returnType.equals(BigInteger.class))
                return (T)new BigInteger((String)obj);
            else if (returnType.equals(BigDecimal.class))
                return (T)new BigDecimal((String)obj);
            else if (returnType.equals(java.sql.Date.class))
                return (T)java.sql.Date.valueOf((String)obj);
            else if (returnType.equals(java.sql.Time.class))
                return (T)java.sql.Time.valueOf((String)obj);
            else if (returnType.equals(java.sql.Timestamp.class))
                return (T)java.sql.Timestamp.valueOf((String)obj);


            return castDynamic(obj,returnType);
        }
        else
        {
            return castDynamic(obj,returnType);
        }
    }

    public static Object getResultSetColumnObject(ResultSet rs,int column,Class<?> targetType) throws SQLException
    {
        // Llamamos al método más apropiado de acuerdo con el tipo de datos esperado
        if (targetType.isPrimitive())
        {
            if (targetType.equals(boolean.class))
                return rs.getBoolean(column);
            else if (targetType.equals(byte.class))
                return rs.getByte(column);
            else if (targetType.equals(char.class))
                return rs.getString(column);
            else if (targetType.equals(short.class))
                return rs.getShort(column);
            else if (targetType.equals(int.class))
                return rs.getInt(column);
            else if (targetType.equals(long.class))
                return rs.getLong(column);
            else if (targetType.equals(float.class))
                return rs.getFloat(column);
            else if (targetType.equals(double.class))
                return rs.getDouble(column);
            else
                return rs.getObject(column); // NO hay más pero por si acaso se añaden más en un futuro Java
        }
        else
        {
            Object obj = rs.getObject(column);
            if (obj == null) return null;

            if (Number.class.isAssignableFrom(targetType))
            {
                if (targetType.equals(Byte.class))
                    return rs.getByte(column);
                else if (targetType.equals(Short.class))
                    return rs.getShort(column);
                else if (targetType.equals(Integer.class))
                    return rs.getInt(column);
                else if (targetType.equals(Long.class))
                    return rs.getLong(column);
                else if (targetType.equals(Float.class))
                    return rs.getFloat(column);
                else if (targetType.equals(Double.class))
                    return rs.getDouble(column);
                else if (targetType.equals(BigDecimal.class))
                    return rs.getBigDecimal(column);
                else
                    return obj; // Puede ser BigInteger
            }
            else if (targetType.equals(Boolean.class))
                return rs.getBoolean(column);
            else if (targetType.equals(Character.class))
                return rs.getString(column);
            else if (targetType.equals(String.class))
                return rs.getString(column);
            else if (targetType.equals(java.sql.Date.class))
                return rs.getDate(column);
            else if (targetType.equals(java.sql.Time.class))
                return rs.getTime(column);
            else if (targetType.equals(java.sql.Timestamp.class))
                return rs.getTimestamp(column);
            else
                return obj;
        }
    }

}
