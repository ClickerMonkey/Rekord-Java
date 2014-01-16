
package org.magnos.rekord.xml;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.magnos.dependency.DependencyAnalyzer;
import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;
import org.magnos.rekord.DefaultTransactionFactory;
import org.magnos.rekord.Field;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.type.TypeBoolean;
import org.magnos.rekord.util.ArrayUtil;
import org.magnos.rekord.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XmlLoader
{

	private static final String TAG_REKORD = "rekord";
	private static final String TAG_DATA_SOURCE = "data-source";
	private static final String TAG_DRIVER = "driver";
	private static final String TAG_JDBC_URL = "jdbc-url";
	private static final String TAG_USER = "user";
	private static final String TAG_PASSWORD = "password";
	private static final String TAG_LOGGING = "logging";
	private static final String TAG_CLASSES = "classes";
	private static final String TAG_TABLE = "table";
	private static final String TAG_FIELDS = "fields";
    private static final String TAG_LOAD_PROFILES = "load-profiles";
    private static final String TAG_LOAD_PROFILE = "load-profile";
    private static final String TAG_SAVE_PROFILES = "save-profiles";
    private static final String TAG_SAVE_PROFILE = "save-profile";
	private static final String TAG_HISTORY = "history";
	private static final String TAG_COLUMN = "column";
	private static final String TAG_FOREIGN_COLUMN = "foreign-column";
	private static final String TAG_ONE_TO_ONE = "one-to-one";
	private static final String TAG_MANY_TO_ONE = "many-to-one";
	private static final String TAG_ONE_TO_MANY = "one-to-many";
	private static final String TAG_INHERITED = "inherited";
	private static final String TAG_CONVERTER_CLASSES = "converter-classes";
	private static final String TAG_CONVERTER_CLASS = "converter-class";
	private static final String TAG_CONVERTERS = "converters";
	private static final String TAG_NATIVE_QUERIES = "native-queries";
	private static final String TAG_QUERY = "query";
	private static final String TAG_LISTENER_CLASSES = "listener-classes";
	private static final String TAG_LISTENER_CLASS = "listener-class";
	private static final String TAG_LISTENERS = "listeners";
	
	private static final Map<String, Integer> LOGGINGS = new HashMap<String, Integer>();
	
	static
	{
		try
		{
			int expectedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
			
			for (java.lang.reflect.Field f : Logging.class.getFields())
			{
				if (f.getType() == int.class && f.getModifiers() == expectedModifiers)
				{
					LOGGINGS.put( f.getName(), f.getInt( null ) );	
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException( e );
		}
	}
	
	public static void load( String classPathFile ) throws Exception
	{
		load( XmlLoader.class.getResourceAsStream( classPathFile ) );
	}
	
	public static void load( InputStream in ) throws Exception
	{
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.parse( new InputSource( in ) );
		new XmlLoader().loadXml( doc.getDocumentElement() );
	}

	private void loadXml( Element element ) throws Exception
	{
		if (!element.getTagName().equalsIgnoreCase( TAG_REKORD ))
		{
			throw new RuntimeException( "Expected tag " + TAG_REKORD );
		}
		
		XmlIterator<Element> nodes = new XmlIterator<Element>( element );
		Map<String, XmlTable> tableMap = new LinkedHashMap<String, XmlTable>();
		Set<String> classesSet = new HashSet<String>();
		Map<String, XmlConverterClass> converterClasses = new HashMap<String, XmlConverterClass>();
		Map<String, XmlListenerClass> listenerClasses = new HashMap<String, XmlListenerClass>();
		Map<String, Converter<?, ?>> converters = new HashMap<String, Converter<?, ?>>();
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			if (tag.equals( TAG_DATA_SOURCE ))
			{
				loadDataSource( e );
			}
			else if (tag.equals( TAG_LOGGING ))
			{
				loadLogging( e );
			}
            else if (tag.equals( TAG_CONVERTER_CLASSES ))
            {
                loadConverterClasses( e, converterClasses );
            }
            else if (tag.equals( TAG_LISTENER_CLASSES ))
            {
            	loadListenerClasses( e, listenerClasses );
            }
            else if (tag.equals( TAG_CONVERTERS ))
            {
                loadConverters( e, converterClasses, converters );
            }
            else if (tag.equals( TAG_CLASSES ))
            {
                loadClasses( e, classesSet );
            }
			else if (tag.equals( TAG_TABLE ))
			{
				XmlTable t = loadTable( e, listenerClasses );
				tableMap.put( t.name, t );
			}
			else
			{
				unexpectedTag( element, e );
			}
		}
		
		List<DependencyNode<Runnable>> dependencyGraph = new ArrayList<DependencyNode<Runnable>>();
		
		for (XmlTable t : tableMap.values()) t.validate( t, tableMap, converters );
		for (XmlTable t : tableMap.values()) t.addNodes( dependencyGraph );
		
		DependencyAnalyzer<Runnable> analyzer = new DependencyAnalyzer<Runnable>();
		
		analyzer.analyze( dependencyGraph );
		
		System.out.println( "Dependency Tree Depth: " + analyzer.getMaximumDepth() );
		
		if (analyzer.isValid())
		{
		    for (Runnable r : analyzer.getOrdered())
		    {
		        if (r != null)
		        {
		            r.run();
		        }
		    }
		}
		else
		{
		    throw new RuntimeException( "Unexpected cyclical dependency in parsing configuration!" ); 
		}
		
		for (String className : classesSet)
		{
			try
			{
				Class.forName( className );
			}
			catch (Exception e)
			{
				throw new RuntimeException( "There was a problem loading the class " + className, e );
			}
		}
	}
	
	private void loadDataSource(Element dataSource) throws Exception
	{
		String driverClass = null;
		String jdbcUrl = null;
		String user = null;
		String password = null;
		
		XmlIterator<Element> nodes = new XmlIterator<Element>( dataSource );
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			if (tag.equals( TAG_DRIVER ))
			{
				driverClass = e.getTextContent();
			}
			else if (tag.equals( TAG_JDBC_URL ))
			{
				jdbcUrl = e.getTextContent();
			}
			else if (tag.equals( TAG_USER ))
			{
				user = e.getTextContent();
			}
			else if (tag.equals( TAG_PASSWORD ))
			{
				password = e.getTextContent();
			}
			else
			{
				unexpectedTag( dataSource, e );
			}
		}
		
		assertNotNull( driverClass, TAG_DRIVER + " element missing in data-source" );
		assertNotNull( jdbcUrl, TAG_JDBC_URL + " element missing in data-source" );
		assertNotNull( user, TAG_USER + " element missing in data-source" );
		assertNotNull( password, TAG_PASSWORD + " element missing in data-source" );
		
		Rekord.setTransactionFactory( new DefaultTransactionFactory( driverClass, jdbcUrl, user, password ) );
	}
	
	private void assertNotNull( Object v, String message )
	{
		if (v == null)
		{
			throw new RuntimeException( message );
		}
	}
	
	private void loadLogging( Element logging )
	{
	    String mode = getAttribute( logging, "mode", "include", true );
	    
	    boolean include = mode.equalsIgnoreCase( "include" );
	    boolean exclude = mode.equalsIgnoreCase( "exclude" );
	    
	    if (!include && !exclude)
	    {
	        throw new RuntimeException( "The only valid values for logging mode are include and exclude" );
	    }
	    
		if (exclude) 
		{
			Rekord.setLogging( true, Logging.ALL );
		}
		
		String[] enabling = split( logging.getTextContent() );
		Integer[] indices = valuesFrom( LOGGINGS, enabling );
		int[] actualIndices = new int[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			actualIndices[i] = indices[i].intValue();
		}
		
		Rekord.setLogging( include, actualIndices );
	}
	
	private void loadClasses( Element classes, Set<String> classSet )
	{
		String[] classNames = split( classes.getTextContent() );
		
		for (String cn : classNames)
		{
			classSet.add( cn );
		}
	}
	
	private void loadConverterClasses( Element converterClassesElement, Map<String, XmlConverterClass> converterClasses ) throws Exception
	{
	    XmlIterator<Element> nodes = new XmlIterator<Element>( converterClassesElement );
	    
	    for (Element e : nodes)
	    {
            String tag = e.getTagName().toLowerCase();
	        
            if (tag.equals( TAG_CONVERTER_CLASS ))
            {
                XmlConverterClass xcc = new XmlConverterClass();
                xcc.converterClassName = getAttribute( e, "class", null, true );
                xcc.elementName = getAttribute( e, "element", null, true ).toLowerCase();
                xcc.converterClass = Class.forName( xcc.converterClassName );
                converterClasses.put( xcc.elementName, xcc );
            }
            else
            {
                unexpectedTag( converterClassesElement, e );
            }
	    }
	}
	
	private void loadListenerClasses( Element listenerClassesElement, Map<String, XmlListenerClass> listenerClasses ) throws Exception
	{
		XmlIterator<Element> nodes = new XmlIterator<Element>( listenerClassesElement );
	    
	    for (Element e : nodes)
	    {
            String tag = e.getTagName().toLowerCase();
	        
            if (tag.equals( TAG_LISTENER_CLASS ))
            {
            	XmlListenerClass xlc = new XmlListenerClass();
            	xlc.elementName = getAttribute( e, "element", null, true ).toLowerCase();
            	xlc.listenerClassName = getAttribute( e, "class", null, true );
            	xlc.listenerClass = Class.forName( xlc.listenerClassName );
            	xlc.listenerEventNames = split( getAttribute( e, "events", null, true ) );
            	
            	ListenerEvent[] eventArray = new ListenerEvent[ xlc.listenerEventNames.length ];
            	
            	for (int i = 0; i < xlc.listenerEventNames.length; i++)
            	{
            		String name = xlc.listenerEventNames[i];
            		ListenerEvent event = ListenerEvent.valueOf( name );
            		
            		if (event == null)
            		{
            			throw new RuntimeException( "Event " + name + " is not valid, must be one of the following values: " + Arrays.toString( ListenerEvent.values() ) );
            		}
            		
            		eventArray[i] = event;
            	}
            	
            	xlc.listenerEvents = eventArray;
            	
            	listenerClasses.put( xlc.elementName, xlc );
            }
            else
            {
                unexpectedTag( listenerClassesElement, e );
            }
	    }
	}
	
	private void loadConverters( Element convertersElement, Map<String, XmlConverterClass> converterClasses, Map<String, Converter<?, ?>> converterMap) throws Exception
	{
	    XmlIterator<Element> nodes = new XmlIterator<Element>( convertersElement );
        
        for (Element e : nodes)
        {
            String tag = e.getTagName().toLowerCase();
            
            XmlConverterClass xcc = converterClasses.get( tag );
            
            if (xcc == null)
            {
                unexpectedTag( convertersElement, e );
                return;
            }

            String name = getAttribute( e, "name", null, true );
            
            Map<String, String> attributes = getAttributes( e );
            attributes.remove( "name" );
            
            Converter<?, ?> converter = xcc.newInstance();
            converter.configure( attributes );
            converterMap.put( name, converter );
        }
	}
	
	private XmlTable loadTable( Element tableElement, Map<String, XmlListenerClass> listeners ) throws Exception
	{
		XmlTable table = new XmlTable();
		table.name = getAttribute( tableElement, "name", null, true );
		table.keyNames = split( getAttribute( tableElement, "key", null, true ) );
		table.lastModifiedColumnsNames = split( getAttribute( tableElement, "last-modified-columns", null, false ) );
		table.extensionName = getAttribute( tableElement, "extends", null, false );
		table.discriminatorColumnName = getAttribute( tableElement, "discriminator", null, false );
		table.discriminatorValueString = getAttribute( tableElement, "discriminator-value", null, false );
		table.dynamicInserts = TypeBoolean.parse( getAttribute( tableElement, "dynamic-insert", "false", true ), "dynamic-insert" );
		table.dynamicUpdates = TypeBoolean.parse( getAttribute( tableElement, "dynamic-update", "true", true ), "dynamic-update" );
		table.transactionCached = TypeBoolean.parse( getAttribute( tableElement, "transaction-cached", "true", true ), "transaction-cached" );
		table.applicationCached = TypeBoolean.parse( getAttribute( tableElement, "application-cached", "false", true ), "application-cached" );
		
		XmlIterator<Element> nodes = new XmlIterator<Element>( tableElement );
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			if (tag.equals( TAG_FIELDS ))
			{
				loadFields( e, table );
			}
            else if (tag.equals( TAG_LOAD_PROFILES ))
            {
                loadLoadProfiles( e, table );
            }
            else if (tag.equals( TAG_SAVE_PROFILES ))
            {
                loadSaveProfiles( e, table );
            }
			else if (tag.equals( TAG_HISTORY ))
			{
				loadHistory( e, table );
			}
			else if (tag.equals( TAG_NATIVE_QUERIES ))
			{
			    loadNativeQueries( e, table );
			}
			else if (tag.equals( TAG_LISTENERS ))
			{
				loadListeners( e, table, listeners );
			}
			else
			{
				unexpectedTag( tableElement, e );
			}
		}
		
		if (!table.loadMap.containsKey( "all" ))
		{
			List<String> fieldNames = new ArrayList<String>();
			
			for (XmlField fn : table.fieldMap.values()) 
			{
				fieldNames.add( fn.name );
			}
			
			XmlLoadProfile loadProfile = new XmlLoadProfile();
			loadProfile.name = "all";
			loadProfile.fieldNames = fieldNames.toArray( new String[ fieldNames.size() ] );
			table.loadMap.put( loadProfile.name, loadProfile );
		}
		
		if (!table.loadMap.containsKey( "id" ))
		{
			XmlLoadProfile loadProfile = new XmlLoadProfile();
			loadProfile.name = "id";
			loadProfile.fieldNames = table.keyNames;
			table.loadMap.put( loadProfile.name, loadProfile );
		}
		
		for (XmlField f : table.fieldMap.values())
		{
			if (!table.loadMap.containsKey( f.name ))
			{
				XmlLoadProfile loadProfile = new XmlLoadProfile();
				loadProfile.name = f.name;
				loadProfile.fieldNames = ArrayUtil.add( f.name, table.keyNames );
				table.loadMap.put( loadProfile.name, loadProfile );
			}
			
			if (!table.saveMap.containsKey( f.name ))
			{
				XmlSaveProfile saveProfile = new XmlSaveProfile();
				saveProfile.name = f.name;
				saveProfile.fieldNames = new String[] {f.name};
				table.saveMap.put( saveProfile.name, saveProfile );
			}
		}
		
		return table;
	}
	
	private void loadFields( Element fields, XmlTable table )
	{
		XmlIterator<Element> nodes = new XmlIterator<Element>( fields );
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			XmlField field = null;
			
			String fieldName = getAttribute( e, "name", null, true );
			
			int otherFlags = 0;
			
			if (tag.equals( TAG_COLUMN ))
			{
				XmlColumn c = new XmlColumn();
				c.sqlType = SqlUtil.getSqlType( getAttribute( e, "sql-type", null, false ) );
				c.typeName = getAttribute( e, "type", null, false );
				c.in = getAttribute( e, "in", "?", true );
				c.out = getAttribute( e, "out", "?", true );
				c.defaultValueString = getAttribute( e, "default-value", null, false );
				c.converterName = getAttribute( e, "converter", null, false );
				field = c;
			}
			else if (tag.equals( TAG_FOREIGN_COLUMN ))
			{
				XmlForeignColumn c = new XmlForeignColumn();
				c.sqlType = SqlUtil.getSqlType( getAttribute( e, "sql-type", null, false ) );
				c.typeName = getAttribute( e, "type", null, false );
				c.in = getAttribute( e, "in", "?", true );
                c.out = getAttribute( e, "out", "?", true );
                c.defaultValueString = getAttribute( e, "default-value", null, false );
                c.converterName = getAttribute( e, "converter", null, false );
                c.foreignTableName = getAttribute( e, "foreign-table", null, true );
                c.foreignColumnName = getAttribute( e, "foreign-column", null, true );
				field = c;
			}
			else if (tag.equals( TAG_ONE_TO_ONE ))
			{
				XmlOneToOne c = new XmlOneToOne();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinLoadName = getAttribute( e, "join-load", "all", true );
				field = c;
				otherFlags = Field.MODEL;
			}
			else if (tag.equals( TAG_MANY_TO_ONE ))
			{
				XmlManyToOne c = new XmlManyToOne();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinLoadName = getAttribute( e, "join-load", "all", true );
				field = c;
				otherFlags = Field.MODEL;
			}
			else if (tag.equals( TAG_ONE_TO_MANY ))
			{
				XmlOneToMany c = new XmlOneToMany();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinLoadName = getAttribute( e, "join-load", "all", true );
				c.fetchSizeString = getAttribute( e, "fetch-size", "128", true );
				c.cascadeDelete = TypeBoolean.parse( getAttribute( e, "cascade-delete", "true", true ), "cascade-delete of field " + fieldName + " in table " + table.name );
				c.cascadeSave = TypeBoolean.parse( getAttribute( e, "cascade-save", "true", true ), "cascade-save of field " + fieldName + " in table " + table.name );
				field = c;
				otherFlags = Field.MODEL_LIST;
			}
			else if (tag.equals( TAG_INHERITED ))
			{
				XmlInheritedColumn c = new XmlInheritedColumn();
				c.table = table;
				c.name = fieldName;
				c.foreignTableName = table.extensionName;
                c.foreignColumnName = getAttribute( e, "column", null, true );
                field = c;
			}
			else
			{
				unexpectedTag( fields, e );
				return;
			}
			
			field.table = table;
			field.name = fieldName;
			field.flags = readFlags( e, field.name, table.name ) | otherFlags;
			field.index = table.fieldMap.size();
			table.fieldMap.put( field.name, field );
		}
		
	}
	
	private int readFlags(Element e, String fieldName, String tableName)
	{
		String lazy = getAttribute( e, "lazy", "false", true );
		String readOnly = getAttribute( e, "read-only", "false", true );
		String generated = getAttribute( e, "generated", "false", true );
		String nonNull = getAttribute( e, "non-null", "false", true );
		String hasDefault = getAttribute( e, "has-default", "false", true );
		String returnOnSave = getAttribute( e, "return-on-save", "false", true );
		String alwaysUpdate = getAttribute( e, "always-update", "false", true );
		
		String messagePostfix = " of field " + fieldName + " in table " + tableName;
		
		return (
			(TypeBoolean.parse( lazy, "lazy" + messagePostfix ) ? Field.LAZY : 0) |
			(TypeBoolean.parse( readOnly, "read-only" + messagePostfix ) ? Field.READ_ONLY : 0) |
			(TypeBoolean.parse( generated, "generated" + messagePostfix ) ? Field.GENERATED | Field.HAS_DEFAULT : 0) |
			(TypeBoolean.parse( nonNull, "non-null" + messagePostfix ) ? Field.NON_NULL : 0) |
			(TypeBoolean.parse( hasDefault, "has-default" + messagePostfix) ? Field.HAS_DEFAULT : 0) |
			(TypeBoolean.parse( returnOnSave, "return-on-save" + messagePostfix) ? Field.RETURN_ON_SAVE : 0) |
			(TypeBoolean.parse( alwaysUpdate, "always-update" + messagePostfix) ? Field.ALWAYS_UPDATE : 0)
		);
	}
    
    private void loadLoadProfiles( Element loadProfiles, XmlTable table )
    {
        XmlIterator<Element> nodes = new XmlIterator<Element>( loadProfiles );
        
        for (Element e : nodes)
        {
            String tag = e.getTagName().toLowerCase();
            
            if (tag.equals( TAG_LOAD_PROFILE ))
            {
                XmlLoadProfile v = new XmlLoadProfile();
                v.name = getAttribute( e, "name", null, true );
                v.fieldNames = split( getAttribute( e, "fields", null, true ) );
                table.loadMap.put( v.name, v );
            }
            else
            {
                unexpectedTag( loadProfiles, e );
            }
        }
    }
    
    private void loadSaveProfiles( Element saveProfiles, XmlTable table )
    {
        XmlIterator<Element> nodes = new XmlIterator<Element>( saveProfiles );
        
        for (Element e : nodes)
        {
            String tag = e.getTagName().toLowerCase();
            
            if (tag.equals( TAG_SAVE_PROFILE ))
            {
                XmlSaveProfile v = new XmlSaveProfile();
                v.name = getAttribute( e, "name", null, true );
                v.fieldNames = split( getAttribute( e, "fields", null, true ) );
                table.saveMap.put( v.name, v );
            }
            else
            {
                unexpectedTag( saveProfiles, e );
            }
        }
    }
	
	private void loadHistory( Element history, XmlTable table )
	{
		table.historyTable = getAttribute( history, "table", null, true );
		table.historyKey = getAttribute( history, "key", null, false );
		table.historyTimestamp = getAttribute( history, "timestamp", null, false );
		table.historyColumnNames = split( getAttribute( history, "columns", null, true ) );
	}
	
	private void loadNativeQueries( Element nativeQueries, XmlTable table )
	{
	    XmlIterator<Element> nodes = new XmlIterator<Element>( nativeQueries );
	    
	    for (Element e : nodes)
	    {
	        String tag = e.getTagName().toLowerCase();
	        
	        if (tag.equals( TAG_QUERY ))
	        {
	            XmlNativeQuery nq = new XmlNativeQuery();
	            nq.name = getAttribute( e, "name", null, true );
	            nq.loadProfile = getAttribute( e, "load", null, false );
	            nq.query = e.getTextContent().trim();
	            table.nativeQueries.add( nq );
	        }
	        else
	        {
	            unexpectedTag( nativeQueries, e );
	        }
	    }
	}
	
	private void loadListeners( Element listenersElement, XmlTable table, Map<String, XmlListenerClass> listeners ) throws Exception
	{
		XmlIterator<Element> nodes = new XmlIterator<Element>( listenersElement );
		    
	    for (Element e : nodes)
	    {
	        String tag = e.getTagName().toLowerCase();
	    	
	    	XmlListenerClass xlc = listeners.get( tag );
	    	
	    	if (xlc != null)
	    	{
	    		XmlListener xl = new XmlListener();
	    		xl.listener = xlc.newInstance();
	    		xl.listenerClass = xlc;
	    		xl.attributes = getAttributes( e );
	    		
	    		table.listeners.add( xl );
	    	}
	        else
	        {
	            unexpectedTag( listenersElement, e );
	        }
	    }
	}
	
	private Map<String, String> getAttributes(Element e)
	{
		Map<String, String> attributes = new HashMap<String, String>();
		
        NamedNodeMap attrs = e.getAttributes();
        
        for (int i = 0; i < attrs.getLength(); i++) 
        {
            Node a = attrs.item( i );
            attributes.put( a.getNodeName(), a.getNodeValue() );
        }
        
        return attributes;
	}
	
	private void unexpectedTag( Element parent, Element child )
	{
		throw new RuntimeException( "Unexpected tag '" + child.getTagName() + "' in element '" + parent.getTagName() + "'");
	}
	
	private String getAttribute(Element e, String name, String defaultValue, boolean required)
	{
		String value = defaultValue;
		
		if (e.hasAttribute( name ))
		{
			value = e.getAttribute( name );
			
			if (value.trim().length() == 0)
			{
			    value = null;
			}
		}
		
		if (value == null && required)
		{
			throw new NullPointerException( "Attribute " + name + " is required and missing from " + e.getTagName() );
		}
		
		return value;
	}
	
	protected static String[] split(String x)
	{
		if (x == null)
		{
			return null;
		}
		
		String[] split = x.split( "[, ]" );

		int nonZero = 0;
		
		for (int i = 0; i < split.length; i++)
		{
			split[i] = split[i].trim();
			
			if (split[i].length() > 0)
			{
				split[nonZero++] = split[i];
			}
		}
		
		return Arrays.copyOf( split, nonZero );
	}
	
	protected static <T> T[] valuesFrom(Map<String, T> map, String[] keys, T ... placeHolder)
	{
		T[] values = Arrays.copyOf( placeHolder, keys.length );
		
		for (int i = 0; i < keys.length; i++)
		{
			values[i] = map.get( keys[i] );
		}
		
		return values;
	}
	
	protected static XmlField[] getFields(XmlTable table, String[] names, String format, Object ... arguments)
	{
		XmlField[] fields = valuesFrom( table.fieldMap, names );
		
		for (int i = 0; i < fields.length; i++)
		{
			if (fields[i] == null)
			{
				throw new RuntimeException( String.format( format, ArrayUtil.join( Object.class, new Object[] {names[i]}, arguments ) ) );
			}
		}
		
		return fields;
	}
	
	protected static <F extends Field<?>> F[] getFields(XmlField[] xmlFields, F ... placeHolder)
	{
		F[] fieldArray = Arrays.copyOf( placeHolder, xmlFields.length );
		
		for (int i = 0; i < xmlFields.length; i++)
		{
			fieldArray[i] = (F)xmlFields[i].field;
		}
		
		return fieldArray;
	}
	
}
