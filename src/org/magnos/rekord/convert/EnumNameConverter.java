
package org.magnos.rekord.convert;

import java.util.Map;

public class EnumNameConverter<T extends Enum<T>> extends AbstractConverter<String, T>
{

    protected Class<T> enumClass;

    @Override
    public T convertFrom( String in )
    {
        return (in == null ? null : Enum.valueOf( enumClass, in ));
    }

    @Override
    public String convertTo( T out )
    {
        return (out == null ? null : out.name());
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {
        enumClass = (Class<T>)Class.forName( attributes.get( "enum" ) );
    }

}
