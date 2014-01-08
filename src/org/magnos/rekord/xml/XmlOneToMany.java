
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.OneToMany;

class XmlOneToMany extends XmlField
{

    String joinTableName;
    String joinLoadName;
    String[] joinKeyNames;
    String fetchSizeString;
    boolean cascadeDelete;
    boolean cascadeSave;

    int fetchSize;
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

        joinKey = XmlLoader.getFields( joinTable, joinKeyNames, "join-key value %s specified for field %s was not found", name );

        loadProfile = joinTable.loadMap.get( joinLoadName );

        if (loadProfile == null)
        {
            throw new RuntimeException( "join-load " + joinLoadName + " specified for field " + name + " was not found" );
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
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
        field = new OneToMany( name, flags, fetchSize, cascadeDelete, cascadeSave );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void relateFieldReferences()
    {
        OneToMany f = (OneToMany)field;
        ForeignColumn<?>[] fcs = XmlLoader.getFields( joinKey );
        f.setJoin( joinTable.table, loadProfile.loadProfile, fcs );
    }
    
}
