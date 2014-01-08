
package org.magnos.rekord.convert;

import java.util.Map;

import org.magnos.rekord.Converter;


public abstract class AbstractConverter<I, O> implements Converter<I, O>
{

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {

    }

}
