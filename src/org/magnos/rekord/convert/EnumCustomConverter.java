
package org.magnos.rekord.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.util.SqlUtil;

public class EnumCustomConverter<T extends Enum<T>> extends AbstractConverter<Object, T>
{

    protected Map<Object, T> enumMap;
    protected Map<T, Object> customMap;

    @Override
    public T convertFrom( Object in )
    {
        if (in == null)
        {
            return null;
        }

        return enumMap.get( in );
    }

    @Override
    public Object convertTo( T out )
    {
        if (out == null)
        {
            return null;
        }

        return customMap.get( out );
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {
        Class<T> enumClass = (Class<T>)Class.forName( attributes.remove( "enum" ) );

        Integer sqlType = SqlUtil.getSqlType( attributes.remove( "custom-type" ) );
        Type<?> customType = Rekord.getType( sqlType );

        enumMap = new HashMap<Object, T>();
        customMap = new HashMap<T, Object>();

        for (Entry<String, String> e : attributes.entrySet())
        {
            T enumConstant = Enum.valueOf( enumClass, e.getKey() );
            Object value = customType.fromString( e.getValue() );

            enumMap.put( value, enumConstant );
            customMap.put( enumConstant, value );
        }
    }

}
