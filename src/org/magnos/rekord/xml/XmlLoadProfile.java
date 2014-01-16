
package org.magnos.rekord.xml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;

class XmlLoadProfile implements XmlLoadable
{
    static final Pattern VIEW_NAME_PATTERN = Pattern.compile( "^([^\\[\\(]+)(|\\(([\\d]+)\\))(|\\[([^\\]]+)\\])$" );
    
    // set from XML
    String name;
    String[] fieldNames;
    
    // set from validate
    XmlFieldLoad[] fieldLoads;
    XmlField[] fields;
    
    // set from Runnable
    LoadProfile loadProfile;

    // nodes
    DependencyNode<Runnable> stateInstantiate = new DependencyNode<Runnable>();
    DependencyNode<Runnable> stateRelateFields = new DependencyNode<Runnable>();
    
    public XmlLoadProfile()
    {
        stateInstantiate.setValue( new Runnable() {
            public void run() {
                Field<?>[] fieldArray = XmlLoader.getFields( fields );
                
                FieldLoad[] fieldLoadArray = new FieldLoad[ fieldLoads.length ];
                
                for (int i = 0; i < fieldLoadArray.length; i++)
                {
                    fieldLoadArray[i] = new FieldLoad();
                    fieldLoadArray[i].setLimit( fieldLoads[i].limitNumber );
                }
                
                loadProfile = new LoadProfile( name, fieldArray, fieldLoadArray );
            }
        } );
        
        stateRelateFields.setValue( new Runnable() {
            public void run() {
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
        } );
    }
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        fields = new XmlField[fieldNames.length];
        fieldLoads = new XmlFieldLoad[table.fieldMap.size()];

        for (int i = 0; i < fieldLoads.length; i++)
        {
        	fieldLoads[i] = new XmlFieldLoad();
        }
        
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
            
            if (loadName != null && f.relatedTable != null) 
            {
                fieldLoads[f.index].loadProfile = f.relatedTable.loadMap.get( loadName ); 
            }
            
            if (limitNumber != null)            
            {
            	fieldLoads[f.index].limitNumber = Integer.parseInt( limitNumber );
            }
        }
    }
    
    @Override
    public void addNodes( List<DependencyNode<Runnable>> nodes )
    {
        for (XmlField f : fields)
        {
            stateInstantiate.addDependency( f.stateInstantiate );
        }
        
        stateRelateFields.addDependency( stateInstantiate );
        
        for (XmlFieldLoad f : fieldLoads)
        {
            if (f.loadProfile != null)
            {
                stateRelateFields.addDependency( f.loadProfile.stateInstantiate );    
            }
        }
        
        nodes.addAll( Arrays.asList( stateInstantiate, stateRelateFields ) );
    }
    
}
