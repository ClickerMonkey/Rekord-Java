
package org.magnos.rekord.xml;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.magnos.rekord.Column;
import org.magnos.rekord.Field;
import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;

class XmlTable extends XmlLoadable
{
    String name;
    String[] keyNames;
    Map<String, XmlField> fieldMap = new LinkedHashMap<String, XmlField>();
    Map<String, XmlView> viewMap = new LinkedHashMap<String, XmlView>();

    String historyTable;
    String historyKey;
    String historyTimestamp;
    String[] historyColumnNames;

    XmlField[] keys;
    XmlField[] historyColumns;
    Class<?> clazz;
    Column<?>[] keyColumns;
    Field<?>[] fields;
    Table<?> table;
    
    @Override
    public void validate(XmlTable table, Map<String, XmlTable> tableMap)
    {
        keys = XmlLoader.getFields( this, keyNames, "key value %s on table %s was not specified in fields", name );
        
        if (historyKey != null && historyKey.trim().length() == 0)
        {
            historyKey = null;
        }
        
        if (historyTimestamp != null && historyTimestamp.trim().length() == 0)
        {
            historyTimestamp = null;
        }
        
        if (historyColumnNames != null)
        {
            historyColumns = XmlLoader.getFields( this, historyColumnNames, "history column %s for table %s was not found in the fields of the table", name );    
        }
        
        for (XmlField f : fieldMap.values()) f.validate( table, tableMap );
        for (XmlView v : viewMap.values()) v.validate( table, tableMap );
    }

    @Override
    public void instantiateFieldImplementation()
    {
        for (XmlField f : fieldMap.values()) f.instantiateFieldImplementation();
    }
    
    @SuppressWarnings ("rawtypes" )
    public void instantiateTableImplementation()
    {
        keyColumns = XmlLoader.getFields( keys );
        table = new Table( name, keyColumns );
    }
    
    @Override
    public void instantiateViewImplementation()
    {
        for (XmlView v : viewMap.values()) v.instantiateViewImplementation();
    }
    
    @Override
    public void initializeTable()
    {
        if (historyColumns != null)
        {
            Column<?>[] columns = XmlLoader.getFields( historyColumns );
            
            table.setHistory( new HistoryTable( historyTable, historyKey, historyTimestamp, columns ) );
        }
        
        Collection<XmlField> fc = fieldMap.values();
        table.setFields( XmlLoader.getFields( fc.toArray( new XmlField[fc.size()] ) ) );
    }
    
    @Override
    public void relateFieldReferences()
    {
        for (XmlView v : viewMap.values()) v.relateFieldReferences();
        for (XmlField f : fieldMap.values()) f.relateFieldReferences();
    }
    
    @Override
    public void finishTable()
    {
        Collection<XmlView> vc = viewMap.values();
        XmlView[] xmlViews = vc.toArray( new XmlView[ vc.size() ] );
        View[] views = new View[ vc.size() ];
        
        for (int i = 0; i < views.length; i++)
        {
            views[i] = xmlViews[i].view;
        }
        
        table.setViews( views );
    }
    
}
