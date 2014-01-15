
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignField;


class XmlForeignField extends XmlColumn
{

    String foreignTableName;
    String foreignColumnName;

    XmlTable foreignTable;
    XmlField foreignColumn;
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        super.validate( table, tableMap );

        validateForeign( tableMap );
    }
    
    protected void validateForeign( Map<String, XmlTable> tableMap )
    {
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

    @Override
    public void relateFieldReferences()
    {
        ForeignField<Object> fc = (ForeignField<Object>)field;
        fc.setForeignColumn( (Column<Object>)foreignColumn.field );
        fc.setForeignTable( foreignTable.table );
    }

}
