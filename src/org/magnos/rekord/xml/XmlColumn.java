
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Type;
import org.magnos.rekord.field.Column;


class XmlColumn extends XmlField
{

    Integer sqlType;
    String in;
    String out;
    Type<?> type;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        if (sqlType == null)
        {
            throw new RuntimeException( "unknown type specified for " + name + " on table " + table.name );
        }
        
        type = XmlLoader.getType( sqlType );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation()
    {
        field = new Column( name, sqlType, type, flags, in, out );
    }
    
}
