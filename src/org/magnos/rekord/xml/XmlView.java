
package org.magnos.rekord.xml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.View;

class XmlView extends XmlLoadable
{
    static final Pattern VIEW_NAME_PATTERN = Pattern.compile( "^([^\\[]+)\\[([^\\]]+)\\]$" );
    
    String name;
    String[] fieldNames;

    XmlField[] fields;
    View view;
    Map<XmlField, XmlView> fieldViews = new LinkedHashMap<XmlField, XmlView>();

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        fields = new XmlField[fieldNames.length];

        for (int i = 0; i < fieldNames.length; i++)
        {
            String fn = fieldNames[i];
            String vn = null;

            Matcher matcher = VIEW_NAME_PATTERN.matcher( fn );

            if (matcher.matches())
            {
                fn = matcher.group( 1 );
                vn = matcher.group( 2 );
            }

            XmlField f = table.fieldMap.get( fn );

            if (f == null)
            {
                throw new RuntimeException( "field " + fn + " for view " + name + " was not found on table " + table.name );
            }

            fields[i] = f;

            if (vn != null)
            {
                fieldViews.put( f, f.relatedTable.viewMap.get( vn ) );
            }
        }
    }

    @Override
    public void instantiateViewImplementation()
    {
        view = new View( name, XmlLoader.getFields( fields ) );
    }

    @Override
    public void relateFieldReferences()
    {
        for (Entry<XmlField, XmlView> e : fieldViews.entrySet())
        {
            view.add( e.getKey().field, e.getValue().view );
        }
    }
    
}
