
package org.magnos.rekord.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.Listener;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.SaveProfile;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.resolve.DiscriminatorModelResolver;
import org.magnos.rekord.resolve.DefaultModelResolver;

class XmlTable implements XmlLoadable
{
	
	// set from XML
    String name;
    String[] keyNames;
    String[] lastModifiedColumnsNames;
    boolean dynamicInserts;
    boolean dynamicUpdates;
    boolean transactionCached;
    boolean applicationCached;
    String extensionName;
    String discriminatorColumnName;
    String discriminatorValueString;
    Map<String, XmlField> fieldMap = new LinkedHashMap<String, XmlField>();
    Map<String, XmlLoadProfile> loadMap = new LinkedHashMap<String, XmlLoadProfile>();
    Map<String, XmlSaveProfile> saveMap = new LinkedHashMap<String, XmlSaveProfile>();
    List<XmlNativeQuery> nativeQueries = new ArrayList<XmlNativeQuery>();
    List<XmlListener> listeners = new ArrayList<XmlListener>();
    String historyTable;
    String historyKey;
    String historyTimestamp;
    String[] historyColumnNames;

    // set from validate
    XmlField[] keys;
    XmlField[] lastModifiedColumns;
    XmlField[] historyColumns;
    XmlTable extension;
    XmlField discriminatorColumn;
    Object discriminatorValue;
    
    // set from Runnable
    int flags;
    Column<?>[] keyColumns;
    Field<?>[] fields;
    Table table;
    
    @Override
    public void validate(XmlTable table, Map<String, XmlTable> tableMap, Map<String, Converter<?, ?>> converters)
    {
        keys = XmlLoader.getFields( this, keyNames, "key value %s on table %s was not specified in fields", name );
        
        if (lastModifiedColumnsNames != null)
        {
        	lastModifiedColumns = XmlLoader.getFields( table, lastModifiedColumnsNames, "column %s on table %s was not specified in fields", name );
        }
        
        if (historyColumnNames != null)
        {
            historyColumns = XmlLoader.getFields( this, historyColumnNames, "history column %s for table %s was not found in the fields of the table", name );    
        }
        
        if (discriminatorColumnName != null)
        {
        	discriminatorColumn = table.fieldMap.get( discriminatorColumnName );
        	
        	if (discriminatorColumn == null)
        	{
        		throw new RuntimeException( "Discriminator column " + discriminatorColumnName + " was not found" );
        	}
        }

        if (extensionName != null)
        {
        	extension = tableMap.get( extensionName );
        }
        
        if (extension == null ^ discriminatorValueString == null)
        {
        	throw new RuntimeException( "If a table extends another it must have a discriminator-value, and if it has a discriminator-value it must extend a table as well" );
        }
        
        for (XmlField f : fieldMap.values()) f.validate( table, tableMap, converters );
        for (XmlLoadProfile v : loadMap.values()) v.validate( table, tableMap );
        for (XmlSaveProfile v : saveMap.values()) v.validate( table, tableMap );
        
        for (XmlNativeQuery nq : nativeQueries)
        {
            if (nq.loadProfile != null && !loadMap.containsKey( nq.loadProfile ))
            {
                throw new RuntimeException( "Native query " + nq.name + " has load profile " + nq.loadProfile + " specified but it was not found in table " + table.name );
            }
        }
    }

    @Override
    public void addNodes(List<DependencyNode<Runnable>> nodes)
    {
    	
    }
    
    @Override
    public void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters)
    {
        for (XmlField f : fieldMap.values()) f.instantiateFieldImplementation(converters);
    }
    
    @Override
    public void instantiateTableImplementation()
    {
    	if (discriminatorValueString != null)
        {
    		Column<?> c = (Column<?>)extension.discriminatorColumn.field;
    		
    		discriminatorValue = c.getConverter().fromDatabase( c.getType().fromString( discriminatorValueString ) );
        }
    	
        flags = (
            (isRelationshipTable() ? Table.RELATIONSHIP_TABLE : 0) |
            (isSubTable() ? Table.SUB_TABLE : 0) |
            (isCompletelyGenerated() ? Table.COMPLETELY_GENERATED : 0) |
            (isDynamicallyInserted() ? Table.DYNAMICALLY_INSERTED : 0) |
            (isDynamicallyUpdated() ? Table.DYNAMICALLY_UPDATED : 0) |
            (isTransactionCached() ? Table.TRANSACTION_CACHED : 0) |
            (isApplicationCached() ? Table.APPLICATION_CACHED : 0)
        );
        
        keyColumns = XmlLoader.getFields( keys );
        
        if (extensionName == null)
        {
        	table = new Table( name, flags, keyColumns );
        	
        	if (discriminatorColumn != null)
            {
            	table.setAsParent( (Column<?>)discriminatorColumn.field );
            }
        }
        else
    	{
    		table = new Table( name, flags, extension.table );
    		
    		if (discriminatorValueString != null)
    		{
    			table.setAsChild( discriminatorValue );
    		}
    	}
    }
    
    @Override
    public void instantiateProfileImplementation()
    {
        for (XmlLoadProfile v : loadMap.values()) v.instantiateProfileImplementation();
        for (XmlSaveProfile v : saveMap.values()) v.instantiateProfileImplementation();
    }
    
    @Override
    public void initializeTable()
    {
        if (historyColumns != null)
        {
            Column<?>[] columns = XmlLoader.getFields( historyColumns );
            
            table.setHistory( new HistoryTable( table, historyTable, historyKey, historyTimestamp, columns ) );
        }
        
        if (lastModifiedColumns != null)
        {
        	Column<?>[] columns = XmlLoader.getFields( lastModifiedColumns );
        	
        	table.setLastModifiedColumns( columns );
        }
        
        Collection<XmlField> fc = fieldMap.values();
        table.setFields( XmlLoader.getFields( fc.toArray( new XmlField[fc.size()] ) ) );
        
        if ( discriminatorColumn == null )
        {
            table.setResolver( new DefaultModelResolver() );
        }
        else
        {
            table.setResolver( new DiscriminatorModelResolver() );
        }
    }
    
    @Override
    public void relateFieldReferences()
    {
        for (XmlLoadProfile v : loadMap.values()) v.relateFieldReferences();
        for (XmlSaveProfile v : saveMap.values()) v.relateFieldReferences();
        for (XmlField f : fieldMap.values()) f.relateFieldReferences();
    }
    
    @Override
    public void finishTable() throws Exception
    {
        Collection<XmlLoadProfile> vc = loadMap.values();
        XmlLoadProfile[] xmlLoadProfiles = vc.toArray( new XmlLoadProfile[ vc.size() ] );
        LoadProfile[] loadProfiles = new LoadProfile[ vc.size() ];
        
        for (int i = 0; i < loadProfiles.length; i++)
        {
            loadProfiles[i] = xmlLoadProfiles[i].loadProfile;
        }
        
        table.setLoadProfiles( loadProfiles );
        
        Collection<XmlSaveProfile> sc = saveMap.values();
        XmlSaveProfile[] xmlSaveProfiles = sc.toArray( new XmlSaveProfile[ sc.size() ] );
        SaveProfile[] saveProfiles = new SaveProfile[ sc.size() ];
        
        for (int i = 0; i < saveProfiles.length; i++)
        {
            saveProfiles[i] = xmlSaveProfiles[i].saveProfile;
        }
        
        table.setSaveProfiles( saveProfiles );
        
        for (XmlNativeQuery nq : nativeQueries)
        {
            table.addNativeQuery( nq.name, nq.query, nq.loadProfile );
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
            if (!(k instanceof XmlForeignField)) 
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
            if (!(k instanceof XmlForeignField)) 
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
