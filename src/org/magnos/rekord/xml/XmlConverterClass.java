package org.magnos.rekord.xml;

import org.magnos.rekord.Converter;


class XmlConverterClass
{
    String elementName;
    String converterClassName;
    Class<?> converterClass;
    
    public Converter<?, ?> newInstance()
    {
        try
        {
            return (Converter<?, ?>)converterClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Converter implementation " + converterClassName + " requires a public no-arg constructor", e );
        }
    }
    
}
