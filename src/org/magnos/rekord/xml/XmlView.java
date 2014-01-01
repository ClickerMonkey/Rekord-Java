
package org.magnos.rekord.xml;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.FieldView;
import org.magnos.rekord.View;

class XmlView extends XmlLoadable
{
    static final Pattern VIEW_NAME_PATTERN = Pattern.compile( "^([^\\[\\(]+)(|\\(([\\d]+)\\))(|\\[([^\\]]+)\\])$" );
    
    String name;
    String[] fieldNames;
    XmlFieldView[] fieldViews;

    XmlField[] fields;
    View view;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        fields = new XmlField[fieldNames.length];
        fieldViews = new XmlFieldView[table.fieldMap.size()];

        for (int i = 0; i < fieldNames.length; i++)
        {
            String fn = fieldNames[i];

            Matcher matcher = VIEW_NAME_PATTERN.matcher( fn );
            
            if (!matcher.matches())
            {
            	throw new RuntimeException( "field name must be in the format of 'field(limit)[sub-view]' where (limit) and [sub-view] are optional" );
            }
            
            String fieldName = matcher.group( 1 );
            String limitNumber = matcher.group( 3 );
            String viewName = matcher.group( 5 );

            XmlField f = table.fieldMap.get( fieldName );

            if (f == null)
            {
                throw new RuntimeException( "field " + fieldName + " for view " + name + " was not found on table " + table.name );
            }

            fields[i] = f;
            
            if (limitNumber != null || viewName != null)
            {
            	fieldViews[i] = new XmlFieldView();
            	
            	if (viewName != null && f.relatedTable != null) {
            		fieldViews[i].view = f.relatedTable.viewMap.get( viewName );	
            	}
            	 
            	fieldViews[i].limitNumber = limitNumber == null ? -1 : Integer.parseInt( limitNumber );
            }
        }
    }

    @Override
    public void instantiateViewImplementation()
    {
        view = new View( name, XmlLoader.getFields( fields ), new FieldView[ fieldViews.length ] );
    }

    @Override
    public void relateFieldReferences()
    {
    	for (int i = 0; i < fieldViews.length; i++)
    	{
    		XmlFieldView xfv = fieldViews[i];
    		
    		if (xfv != null)
    		{
    			FieldView fv = new FieldView();
    			fv.setView( xfv.view != null ? xfv.view.view : null );
    			fv.setLimit( xfv.limitNumber );
    			view.getFieldViews()[i] = fv;
    		}
    	}
    }
    
}
