
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Column;


class XmlColumn extends XmlField
{

    Integer type;

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
        field = new Column( name, type, flags );
    }
    
}
