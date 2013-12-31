
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.field.Column;


class XmlColumn extends XmlField
{

    Integer sqlType;
    String in;
    String out;
    Type<?> type;
    String defaultValueString;
    
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
    public void instantiateFieldImplementation()
    {
        field = new Column( name, sqlType, type, flags, in, out, defaultValue );
    }
    
}
