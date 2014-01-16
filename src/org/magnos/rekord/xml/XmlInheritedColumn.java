
package org.magnos.rekord.xml;

import java.util.List;
import java.util.Map;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.InheritColumn;


class XmlInheritedColumn extends XmlForeignField
{

    public XmlInheritedColumn()
    {
        stateInstantiate.setValue( new Runnable() {

            @SuppressWarnings ("rawtypes" )
            public void run()
            {
                field = new InheritColumn( name, (Column<?>)foreignColumn.field );
            }
        } );
    }

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        validateForeign( tableMap );
    }

    @Override
    public void addNodes( List<DependencyNode<Runnable>> nodes )
    {
        super.addNodes( nodes );

        stateInstantiate.addDependency( foreignColumn.stateInstantiate );
    }

}
