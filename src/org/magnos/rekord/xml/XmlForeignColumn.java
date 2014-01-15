
package org.magnos.rekord.xml;

import java.sql.Types;
import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.ForeignColumn;


class XmlForeignColumn extends XmlForeignField
{

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
    	Converter convert = converters.get( converterName );
    	
    	if (convert == null)
    	{
    		convert = NoConverter.INSTANCE;
    	}
    	else
    	{
    		defaultValue = convert.fromDatabase( defaultValue );
    	}
    	
    	if (sqlType == null)
    	{
    	    sqlType = Types.OTHER;
    	}
    	
        field = new ForeignColumn( name, sqlType, type, in, out, defaultValue, convert );
    }

}
