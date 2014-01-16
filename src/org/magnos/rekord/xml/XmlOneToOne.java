
package org.magnos.rekord.xml;

import org.magnos.rekord.field.OneToOne;


class XmlOneToOne extends XmlJoinField
{

    public XmlOneToOne()
    {
        stateInstantiate.setValue( new Runnable() {

            @SuppressWarnings ("rawtypes" )
            public void run()
            {
                field = new OneToOne( name, flags );
            }
        } );
    }

}
