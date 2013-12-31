
package org.magnos.rekord.convert;

import java.util.Map;


@SuppressWarnings ("rawtypes" )
public class EnumNameConverter extends AbstractConverter<String, Enum>
{

    protected Class<Enum> enumClass;

    @Override
    public Enum<?> convertFrom( String in )
    {
        return (in == null ? null : Enum.valueOf( enumClass, in ));
    }

    @Override
    public String convertTo( Enum out )
    {
        return (out == null ? null : out.name());
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {
        enumClass = (Class<Enum>)Class.forName( attributes.get( "enum" ) );
    }

}
