
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.ManyToOne;

class XmlManyToOne extends XmlField
{

    String joinTableName;
    String joinLoadName;
    String[] joinKeyNames;

    XmlTable joinTable;
    XmlField[] joinKey;
    XmlLoadProfile loadProfile;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        joinTable = tableMap.get( joinTableName );

        if (joinTable == null)
        {
            throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
        }

        joinKey = XmlLoader.getFields( table, joinKeyNames, "join-key value %s specified for field %s was not found", name );

        loadProfile = joinTable.loadMap.get( joinLoadName );

        if (loadProfile == null)
        {
            throw new RuntimeException( "join-load " + joinLoadName + " specified for field " + name + " was not found" );
        }

        relatedTable = joinTable;
    }
    
    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
        field = new ManyToOne( name, flags );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void relateFieldReferences()
    {
        ManyToOne f = (ManyToOne)field;
        ForeignColumn<?>[] fcs = XmlLoader.getFields( joinKey );
        f.setJoin( joinTable.table, loadProfile.loadProfile, fcs );
    }
    
}
