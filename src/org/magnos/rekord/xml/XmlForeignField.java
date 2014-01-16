
package org.magnos.rekord.xml;

import java.util.List;
import java.util.Map;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignField;


class XmlForeignField extends XmlColumn
{

    // set from XML
    String foreignTableName;
    String foreignColumnName;

    // set from validate
    XmlTable foreignTable;
    XmlField foreignColumn;

    public XmlForeignField()
    {
        stateRelateFields.setValue( new Runnable() {

            public void run()
            {
                ForeignField<Object> fc = (ForeignField<Object>)field;
                fc.setForeignColumn( (Column<Object>)foreignColumn.field );
                fc.setForeignTable( foreignTable.table );
            }
        } );
    }

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        super.validate( table, tableMap, converters );

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

    public void addNodes( List<DependencyNode<Runnable>> nodes )
    {
        super.addNodes( nodes );

        stateRelateFields.addDependency( foreignTable.stateInstantiate );
        stateRelateFields.addDependency( foreignColumn.stateInstantiate );
    }

}
