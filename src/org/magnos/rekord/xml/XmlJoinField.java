
package org.magnos.rekord.xml;

import java.util.List;
import java.util.Map;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.JoinField;


class XmlJoinField extends XmlField
{

    // set from XML
    String joinTableName;
    String joinLoadName;
    String[] joinKeyNames;

    // set from validate
    XmlTable joinTable;
    XmlField[] joinKey;
    XmlLoadProfile loadProfile;

    public XmlJoinField()
    {
        stateRelateFields.setValue( new Runnable() {

            public void run()
            {
                JoinField<?> joinField = (JoinField<?>)field;
                ForeignColumn<?>[] fcs = XmlLoader.getFields( joinKey );
                joinField.setJoin( joinTable.table, loadProfile.loadProfile, fcs );
            }
        } );
    }

    protected XmlTable getForeignTable()
    {
        return table;
    }
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        joinTable = tableMap.get( joinTableName );
        relatedTable = joinTable;
        
        if (joinTable == null)
        {
            throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
        }

        joinKey = XmlLoader.getFields( getForeignTable(), joinKeyNames, "join-key value %s specified for field %s was not found", name );

        loadProfile = joinTable.loadMap.get( joinLoadName );

        if (loadProfile == null)
        {
            throw new RuntimeException( "join-load " + joinLoadName + " specified for field " + name + " was not found" );
        }

    }

    @Override
    public void addNodes( List<DependencyNode<Runnable>> nodes )
    {
        super.addNodes( nodes );

        stateRelateFields.addDependencies( joinTable.stateInstantiate, loadProfile.stateInstantiate );

        for (XmlField f : joinKey)
        {
            stateRelateFields.addDependency( f.stateInstantiate );
        }
    }

}
