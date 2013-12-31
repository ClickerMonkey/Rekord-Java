
package org.magnos.rekord.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.util.SqlUtil;

@SuppressWarnings ("rawtypes" )
public class EnumCustomConverter extends AbstractConverter<Object, Enum>
{

    protected Map<Object, Enum> enumMap;
    protected Map<Enum, Object> customMap;

    @Override
    public Enum convertFrom( Object in )
    {
        if (in == null)
        {
            return null;
        }

        return enumMap.get( in );
    }

    @Override
    public Object convertTo( Enum out )
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
        Class<Enum> enumClass = (Class<Enum>)Class.forName( attributes.remove( "enum" ) );

        Integer sqlType = SqlUtil.getSqlType( attributes.remove( "custom-type" ) );
        Type<?> customType = Rekord.getType( sqlType );

        enumMap = new HashMap<Object, Enum>();
        customMap = new HashMap<Enum, Object>();

        for (Entry<String, String> e : attributes.entrySet())
        {
            Object key = customType.fromString( e.getKey() );
            Enum enumConstant = Enum.valueOf( enumClass, e.getValue() );

            enumMap.put( key, enumConstant );
            customMap.put( enumConstant, key );
        }
    }

}
