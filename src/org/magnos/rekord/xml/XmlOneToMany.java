
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.OneToMany;

class XmlOneToMany extends XmlField
{

    String joinTableName;
    String joinViewName;
    String[] joinKeyNames;
    String fetchSizeString;

    int fetchSize;
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

        joinKey = XmlLoader.getFields( joinTable, joinKeyNames, "join-key value %s specified for field %s was not found", name );

        view = joinTable.viewMap.get( joinViewName );

        if (view == null)
        {
            throw new RuntimeException( "join-view " + joinViewName + " specified for field " + name + " was not found" );
        }

        relatedTable = joinTable;

        try
        {
            fetchSize = Integer.parseInt( fetchSizeString );
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException( "fetch-size is not a valid number: " + fetchSizeString, e );
        }

        if (fetchSize <= 0)
        {
            throw new RuntimeException( "fetch-size must be a positive number greater than zero: " + fetchSize );
        }
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation()
    {
        field = new OneToMany( name, flags, fetchSize );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void relateFieldReferences()
    {
        OneToMany f = (OneToMany)field;
        ForeignColumn<?>[] fcs = XmlLoader.getFields( joinKey );
        f.setJoin( joinTable.table, view.view, fcs );
    }
    
}
