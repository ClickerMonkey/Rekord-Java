
package org.magnos.rekord;

import java.util.Map;


public interface Converter<I, O>
{
    public O fromDatabase( I in );

    public I toDatabase( O out );

    public void configure( Map<String, String> attributes ) throws Exception;
}
