
package org.magnos.rekord.convert;

import java.util.Map;

@SuppressWarnings ("rawtypes" )
public class EnumOrdinalConverter extends AbstractConverter<Number, Enum>
{

    protected Class<Enum> enumClass;
    protected Enum[] enumConstants;

    @Override
    public Enum convertFrom( Number in )
    {
        return (in == null ? null : enumConstants[in.intValue()]);
    }

    @Override
    public Number convertTo( Enum out )
    {
        return (out == null ? null : out.ordinal());
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {
        enumClass = (Class<Enum>)Class.forName( attributes.get( "enum" ) );
        enumConstants = enumClass.getEnumConstants();
    }

}
