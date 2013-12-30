
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Formula;

class XmlFormula extends XmlField
{
    
    String equation;
    String alias;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
    }

    @SuppressWarnings ("rawtypes" )
    @Override
    public void instantiateFieldImplementation()
    {
        field = new Formula( name, equation, alias, flags );
    }

}
