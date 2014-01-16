
package org.magnos.rekord.xml;

import java.sql.Types;
import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;

@SuppressWarnings ("rawtypes" )
class XmlColumn extends XmlField
{

	// set from XML
    Integer sqlType;
    String typeName;
    String in;
    String out;
    Type<?> type;
    String defaultValueString;
    String converterName;
    
    // set from validation
    Object defaultValue;
	Converter converter;

    public XmlColumn()
    {
    	nodeInstantiate.setValue( new Runnable() {
    		public void run() {
    			field = new Column( name, sqlType, type, flags, in, out, defaultValue, converter );
    		}
    	});
    }
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        if (sqlType == null && typeName == null)
        {
            throw new RuntimeException( "unknown type specified for " + name + " on table " + table.name );
        }
        
        type = sqlType != null ? Rekord.getType( sqlType ) : Rekord.getType( typeName );
        
        if (sqlType == null)
    	{
    	    sqlType = Types.OTHER;
    	}
        
        converter = converters.get( converterName );
    	
    	if (converter == null)
    	{
    		converter = NoConverter.INSTANCE;
    	}
    	else
    	{
    		defaultValue = converter.fromDatabase( type.fromString( defaultValueString ) );
    	}
    }

}
