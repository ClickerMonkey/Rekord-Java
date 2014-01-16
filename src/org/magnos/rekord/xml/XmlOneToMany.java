
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.field.OneToMany;


class XmlOneToMany extends XmlJoinField
{

    // set from XML
    String fetchSizeString;
    boolean cascadeDelete;
    boolean cascadeSave;

    // set from validate
    int fetchSize;

    public XmlOneToMany()
    {
        stateInstantiate.setValue( new Runnable() {

            @SuppressWarnings ("rawtypes" )
            public void run()
            {
                field = new OneToMany( name, flags, fetchSize, cascadeDelete, cascadeSave );
            }
        } );
    }

    protected XmlTable getForeignTable()
    {
        return relatedTable;
    }

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        super.validate( table, tableMap, converters );

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

}
