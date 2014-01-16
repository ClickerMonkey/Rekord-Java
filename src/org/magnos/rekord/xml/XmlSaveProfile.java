
package org.magnos.rekord.xml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.SaveProfile;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.UpdateQuery;

class XmlSaveProfile implements XmlLoadable
{
    
    // set from XML
    String name;
    String[] fieldNames;

    // set from validate
    XmlTable xmlTable;
    XmlField[] fields;
    
    // set from Runnable
    SaveProfile saveProfile;

    // nodes
    DependencyNode<Runnable> stateInstantiate = new DependencyNode<Runnable>();

    public XmlSaveProfile()
    {
        stateInstantiate.setValue( new Runnable() {
            public void run() {
                Field<?>[] fieldArray = XmlLoader.getFields( fields );
                
                saveProfile = new SaveProfile( name, fieldArray, InsertQuery.forFields( xmlTable.table, fieldArray ), UpdateQuery.forFields( xmlTable.table, fieldArray ) );
            }
        } );
    }
    
    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters )
    {
        xmlTable = table;
        fields = new XmlField[fieldNames.length];

        for (int i = 0; i < fieldNames.length; i++)
        {
            String fn = fieldNames[i];
            XmlField f = table.fieldMap.get( fn );
            
            if (f == null)
            {
                throw new RuntimeException( "field " + fn + " for load " + name + " was not found on table " + table.name );
            }

            fields[i] = f;
        }
    }

    @Override
    public void addNodes( List<DependencyNode<Runnable>> nodes )
    {
        stateInstantiate.addDependency( xmlTable.stateInstantiate );
        
        for (XmlField f : fields)
        {
            stateInstantiate.addDependency( f.stateInstantiate );
        }
        
        nodes.addAll( Arrays.asList( stateInstantiate ) );
    }

}
