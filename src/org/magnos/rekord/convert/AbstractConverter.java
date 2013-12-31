
package org.magnos.rekord.convert;

import java.util.Map;

import org.magnos.rekord.Converter;


public abstract class AbstractConverter<I, O> implements Converter<I, O>
{

    protected String name;
    
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public void configure( Map<String, String> attributes ) throws Exception
    {

    }

}
