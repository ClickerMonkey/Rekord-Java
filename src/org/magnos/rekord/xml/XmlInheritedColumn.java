
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.InheritColumn;


class XmlInheritedColumn extends XmlForeignField
{

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
    	validateForeign( tableMap );
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
        field = new InheritColumn( name, (Column<?>)foreignColumn.field );
    }

}
