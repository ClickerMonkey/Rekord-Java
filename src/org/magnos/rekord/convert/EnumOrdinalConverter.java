
package org.magnos.rekord.convert;

import java.util.Map;

public class EnumOrdinalConverter<T extends Enum<T>> extends AbstractConverter<Number, T>
{

    protected T[] enumConstants;

    @Override
    public T fromDatabase( Number in )
    {
        return (in == null ? null : enumConstants[in.intValue()]);
    }

    @Override
    public Number toDatabase( T out )
    {
        return (out == null ? null : out.ordinal());
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {
        Class<T> enumClass = (Class<T>)Class.forName( attributes.get( "enum" ) );
        enumConstants = enumClass.getEnumConstants();
    }

}
