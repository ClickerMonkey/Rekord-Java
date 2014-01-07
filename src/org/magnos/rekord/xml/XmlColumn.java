
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;


class XmlColumn extends XmlField
{

    Integer sqlType;
    String in;
    String out;
    Type<?> type;
    String defaultValueString;
    String converterName;
    
    Object defaultValue;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        if (sqlType == null)
        {
            throw new RuntimeException( "unknown type specified for " + name + " on table " + table.name );
        }
        
        type = Rekord.getType( sqlType );
        
        defaultValue = type.fromString( defaultValueString );
    }

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
    	
        field = new Column( name, sqlType, type, flags, in, out, defaultValue, convert );
    }
    
}
