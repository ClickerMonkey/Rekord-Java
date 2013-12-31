
package org.magnos.rekord;

import java.util.Map;


public interface Converter<I, O>
{
    public String getName();

    public void setName( String name );

    public O convertFrom( I in );

    public I convertTo( O out );

    public void configure( Map<String, String> attributes ) throws Exception;
}
