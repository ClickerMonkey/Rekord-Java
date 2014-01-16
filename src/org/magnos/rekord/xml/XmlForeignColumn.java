
package org.magnos.rekord.xml;

import org.magnos.rekord.field.ForeignColumn;


class XmlForeignColumn extends XmlForeignField
{

    public XmlForeignColumn()
    {
        stateInstantiate.setValue( new Runnable() {

            @SuppressWarnings ("rawtypes" )
            public void run()
            {
                field = new ForeignColumn( name, sqlType, type, in, out, defaultValue, converter );
            }
        } );
    }

}
