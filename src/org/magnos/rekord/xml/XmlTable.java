
package org.magnos.rekord.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.Listener;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;
import org.magnos.rekord.field.Column;

class XmlTable extends XmlLoadable
{
    String name;
    String[] keyNames;
    boolean dynamicInserts;
    boolean dynamicUpdates;
    boolean transactionCached;
    boolean applicationCached;
    Map<String, XmlField> fieldMap = new LinkedHashMap<String, XmlField>();
    Map<String, XmlView> viewMap = new LinkedHashMap<String, XmlView>();
    List<XmlNativeQuery> nativeQueries = new ArrayList<XmlNativeQuery>();
    List<XmlListener> listeners = new ArrayList<XmlListener>();
    
    String historyTable;
    String historyKey;
    String historyTimestamp;
    String[] historyColumnNames;

    XmlField[] keys;
    XmlField[] historyColumns;
    Class<?> clazz;
    Column<?>[] keyColumns;
    Field<?>[] fields;
    Table table;
    
    @Override
    public void validate(XmlTable table, Map<String, XmlTable> tableMap)
    {
        keys = XmlLoader.getFields( this, keyNames, "key value %s on table %s was not specified in fields", name );
        
        if (historyColumnNames != null)
        {
            historyColumns = XmlLoader.getFields( this, historyColumnNames, "history column %s for table %s was not found in the fields of the table", name );    
        }
        
        for (XmlField f : fieldMap.values()) f.validate( table, tableMap );
        for (XmlView v : viewMap.values()) v.validate( table, tableMap );
        
        for (XmlNativeQuery nq : nativeQueries)
        {
            if (nq.view != null && !viewMap.containsKey( nq.view ))
            {
                throw new RuntimeException( "Native query " + nq.name + " has view " + nq.view + " specified but it was not found in table " + table.name );
            }
        }
    }

    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
        for (XmlField f : fieldMap.values()) f.instantiateFieldImplementation(converters);
    }
    
    @Override
    public void instantiateTableImplementation()
    {
        int flags = (
            (isRelationshipTable() ? Table.RELATIONSHIP_TABLE : 0) |
            (isSubTable() ? Table.SUB_TABLE : 0) |
            (isCompletelyGenerated() ? Table.COMPLETELY_GENERATED : 0) |
            (isDynamicallyInserted() ? Table.DYNAMICALLY_INSERTED : 0) |
            (isDynamicallyUpdated() ? Table.DYNAMICALLY_UPDATED : 0) |
            (isTransactionCached() ? Table.TRANSACTION_CACHED : 0) |
            (isApplicationCached() ? Table.APPLICATION_CACHED : 0)
        );
        
        keyColumns = XmlLoader.getFields( keys );
        table = new Table( name, flags, keyColumns );
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
            
            table.setHistory( new HistoryTable( table, historyTable, historyKey, historyTimestamp, columns ) );
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
    public void finishTable() throws Exception
    {
        Collection<XmlView> vc = viewMap.values();
        XmlView[] xmlViews = vc.toArray( new XmlView[ vc.size() ] );
        View[] views = new View[ vc.size() ];
        
        for (int i = 0; i < views.length; i++)
        {
            views[i] = xmlViews[i].view;
        }
        
        table.setViews( views );
        
        for (XmlNativeQuery nq : nativeQueries)
        {
            table.addNativeQuery( nq.name, nq.query, nq.view );
        }
        
        for (XmlListener xl : listeners)
        {
        	xl.listener.configure( table, xl.attributes );
        	
        	for (ListenerEvent le : xl.listenerClass.listenerEvents)
        	{
        		table.addListener( (Listener<Model>)xl.listener, le );
        	}
        }
    }
    
    private boolean isRelationshipTable()
    {
        Set<XmlTable> related = new HashSet<XmlTable>();
        
        for (XmlField k : keys)
        {
            if (!(k instanceof XmlForeignColumn)) 
            {
                return false;
            }
            
            related.add( k.table );
        }
        
        return related.size() > 1;
    }
    
    private boolean isSubTable()
    {
        Set<XmlTable> related = new HashSet<XmlTable>();
        
        for (XmlField k : keys)
        {
            if (!(k instanceof XmlForeignColumn)) 
            {
                return false;
            }
            
            related.add( k.table );
        }
        
        return related.size() == 1;
    }
    
    private boolean isCompletelyGenerated()
    {
        for (XmlField f : fieldMap.values())
        {
            if (f instanceof XmlColumn)
            {
                XmlColumn c = (XmlColumn)f;
                
                if (c.defaultValue == null && (c.flags & Field.GENERATED) == 0)
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean isDynamicallyInserted()
    {
    	return dynamicInserts;
    }
    
    private boolean isDynamicallyUpdated()
    {
    	return dynamicUpdates;
    }
    
    private boolean isTransactionCached()
    {
    	return transactionCached;
    }
    
    private boolean isApplicationCached()
    {
    	return applicationCached;
    }
    
}
