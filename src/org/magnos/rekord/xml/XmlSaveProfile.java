
package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Field;
import org.magnos.rekord.SaveProfile;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.UpdateQuery;

class XmlSaveProfile extends XmlLoadable
{
    String name;
    String[] fieldNames;
    XmlTable xmlTable;
        
    XmlField[] fields;
    SaveProfile saveProfile;

    @Override
    public void validate( XmlTable table, Map<String, XmlTable> tableMap )
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
    public void relateFieldReferences()
    {
        Field<?>[] fieldArray = XmlLoader.getFields( fields );
        
        saveProfile = new SaveProfile( name, fieldArray, InsertQuery.forFields( xmlTable.table, fieldArray ), UpdateQuery.forFields( xmlTable.table, fieldArray ) );
    }

}
