
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;


class XmlForeignColumn extends XmlColumn
{

    String foreignTableName;
    String foreignColumnName;

    XmlTable foreignTable;
    XmlField foreignColumn;
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        super.validate( table, tableMap );

        foreignTable = tableMap.get( foreignTableName );

        if (foreignTable == null)
        {
            throw new RuntimeException( "foreign-table " + foreignTableName + " specified for field " + name + " was not found" );
        }

        foreignColumn = foreignTable.fieldMap.get( foreignColumnName );

        if (foreignColumn == null)
        {
            throw new RuntimeException( "foreign-column " + foreignColumnName + " specified for field " + name + " was not found" );
        }

        relatedTable = foreignTable;
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
    	Converter convert = converters.get( converterName );
    	
    	if (convert == null)
    	{
    		convert = NoConverter.INSTANCE;
    	}
    	else
    	{
    		defaultValue = convert.fromDatabase( defaultValue );
    	}
    	
        field = new ForeignColumn( name, sqlType, type, in, out, defaultValue, convert );
    }

    @Override
    public void relateFieldReferences()
    {
        ForeignColumn<Object> fc = (ForeignColumn<Object>)field;
        fc.setForeignColumn( (Column<Object>)foreignColumn.field );
        fc.setForeignTable( foreignTable.table );
    }

}
