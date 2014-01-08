
package org.magnos.rekord.xml;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;

class XmlLoadProfile extends XmlLoadable
{
    static final Pattern VIEW_NAME_PATTERN = Pattern.compile( "^([^\\[\\(]+)(|\\(([\\d]+)\\))(|\\[([^\\]]+)\\])$" );
    
    String name;
    String[] fieldNames;
    XmlFieldLoad[] fieldLoads;

    XmlField[] fields;
    LoadProfile loadProfile;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
    {
        fields = new XmlField[fieldNames.length];
        fieldLoads = new XmlFieldLoad[table.fieldMap.size()];

        for (int i = 0; i < fieldNames.length; i++)
        {
            String fn = fieldNames[i];

            Matcher matcher = VIEW_NAME_PATTERN.matcher( fn );
            
            if (!matcher.matches())
            {
            	throw new RuntimeException( "field name must be in the format of 'field(limit)[sub-load]' where (limit) and [sub-load] are optional" );
            }
            
            String fieldName = matcher.group( 1 );
            String limitNumber = matcher.group( 3 );
            String loadName = matcher.group( 5 );

            XmlField f = table.fieldMap.get( fieldName );

            if (f == null)
            {
                throw new RuntimeException( "field " + fieldName + " for load " + name + " was not found on table " + table.name );
            }

            fields[i] = f;
            
            if (limitNumber != null || loadName != null)
            {
            	fieldLoads[i] = new XmlFieldLoad();
            	
            	if (loadName != null && f.relatedTable != null) {
            		fieldLoads[i].loadProfile = f.relatedTable.loadMap.get( loadName );	
            	}
            	 
            	fieldLoads[i].limitNumber = limitNumber == null ? -1 : Integer.parseInt( limitNumber );
            }
        }
    }

    @Override
    public void instantiateLoadProfileImplementation()
    {
    	Field<?>[] fieldArray = XmlLoader.getFields( fields );
    	
    	FieldLoad[] fieldLoadArray = new FieldLoad[ fieldLoads.length ];
    	
    	for (int i = 0; i < fieldArray.length; i++)
    	{
    		fieldLoadArray[i] = new FieldLoad();
    	}
    	
        loadProfile = new LoadProfile( name, fieldArray, fieldLoadArray );
    }

    @Override
    public void relateFieldReferences()
    {
    	for (int i = 0; i < fieldLoads.length; i++)
    	{
    		XmlFieldLoad xfv = fieldLoads[i];
    		FieldLoad fv = loadProfile.getFieldLoads()[i];
    		
    		if (xfv != null)
    		{
    			if (xfv.loadProfile != null)
    			{
    				fv.setLoadProfile( xfv.loadProfile.loadProfile );
    			}
    			
    			fv.setLimit( xfv.limitNumber );
    		}
    	}
    }
    
}
