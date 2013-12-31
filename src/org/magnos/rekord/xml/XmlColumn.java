
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.field.Column;


class XmlColumn extends XmlField
{

    Integer type;
    String in;
    String out;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        if (type == null)
        {
            throw new RuntimeException( "unknown type specified for " + name + " on table " + table.name );
        }
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation()
    {
        field = new Column( name, type, flags, in, out );
    }
    
}
