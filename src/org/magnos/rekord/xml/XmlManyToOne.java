
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.ManyToOne;

class XmlManyToOne extends XmlField
{

    String joinTableName;
    String joinViewName;
    String[] joinKeyNames;

    XmlTable joinTable;
    XmlField[] joinKey;
    XmlView view;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        joinTable = tableMap.get( joinTableName );

        if (joinTable == null)
        {
            throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
        }

        joinKey = XmlLoader.getFields( table, joinKeyNames, "join-key value %s specified for field %s was not found", name );

        view = joinTable.viewMap.get( joinViewName );

        if (view == null)
        {
            throw new RuntimeException( "join-view " + joinViewName + " specified for field " + name + " was not found" );
        }

        relatedTable = joinTable;
    }
    
    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation()
    {
        field = new ManyToOne( name, flags );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void relateFieldReferences()
    {
        ManyToOne f = (ManyToOne)field;
        ForeignColumn<?>[] fcs = XmlLoader.getFields( joinKey );
        f.setJoin( joinTable.table, view.view, fcs );
    }
    
}
