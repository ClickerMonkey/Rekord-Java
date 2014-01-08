
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
            fieldLoads[i] = new XmlFieldLoad();
            
            if (loadName != null && f.relatedTable != null) 
            {
                fieldLoads[i].loadProfile = f.relatedTable.loadMap.get( loadName ); 
            }
            
            if (limitNumber != null)            
            {
            	fieldLoads[i].limitNumber = Integer.parseInt( limitNumber );
            }
        }
    }

    @Override
    public void instantiateProfileImplementation()
    {
    	Field<?>[] fieldArray = XmlLoader.getFields( fields );
    	
    	FieldLoad[] fieldLoadArray = new FieldLoad[ fieldLoads.length ];
    	
    	for (int i = 0; i < fieldArray.length; i++)
    	{
    		fieldLoadArray[i] = new FieldLoad();
    		fieldLoadArray[i].setLimit( fieldLoads[i].limitNumber );
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
    		
    		if (xfv != null && xfv.loadProfile != null)
    		{
   				fv.setLoadProfile( xfv.loadProfile.loadProfile );
    		}
    	}
    }
    
}
