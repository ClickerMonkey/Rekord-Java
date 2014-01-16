
package org.magnos.rekord.xml;

import org.magnos.rekord.field.ManyToOne;


class XmlManyToOne extends XmlJoinField
{

    public XmlManyToOne()
    {
        stateInstantiate.setValue( new Runnable() {

            @SuppressWarnings ("rawtypes" )
            public void run()
            {
                field = new ManyToOne( name, flags );
            }
        } );
    }

}
