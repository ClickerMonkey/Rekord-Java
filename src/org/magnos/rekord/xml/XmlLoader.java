
package org.magnos.rekord.xml;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.sql.Types;
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

import org.magnos.rekord.DefaultTransactionFactory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Flags;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.type.TypeArray;
import org.magnos.rekord.type.TypeBlob;
import org.magnos.rekord.type.TypeBoolean;
import org.magnos.rekord.type.TypeByte;
import org.magnos.rekord.type.TypeByteArray;
import org.magnos.rekord.type.TypeClob;
import org.magnos.rekord.type.TypeDate;
import org.magnos.rekord.type.TypeDecimal;
import org.magnos.rekord.type.TypeDouble;
import org.magnos.rekord.type.TypeFloat;
import org.magnos.rekord.type.TypeInteger;
import org.magnos.rekord.type.TypeLong;
import org.magnos.rekord.type.TypeObject;
import org.magnos.rekord.type.TypeRef;
import org.magnos.rekord.type.TypeRowId;
import org.magnos.rekord.type.TypeShort;
import org.magnos.rekord.type.TypeString;
import org.magnos.rekord.type.TypeStruct;
import org.magnos.rekord.type.TypeTime;
import org.magnos.rekord.type.TypeTimestamp;
import org.magnos.rekord.type.TypeXml;
import org.magnos.rekord.util.ArrayUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	private static final String TAG_VIEWS = "views";
	private static final String TAG_HISTORY = "history";
	private static final String TAG_VIEW = "view";
	private static final String TAG_COLUMN = "column";
	private static final String TAG_FOREIGN_COLUMN = "foreign-column";
	private static final String TAG_ONE_TO_ONE = "one-to-one";
	private static final String TAG_MANY_TO_ONE = "many-to-one";
	private static final String TAG_ONE_TO_MANY = "one-to-many"; 
	
	private static final Map<String, Integer> SQL_TYPES = new HashMap<String, Integer>();
	private static final Map<String, Integer> LOGGINGS = new HashMap<String, Integer>();
	
	static
	{
		try
		{
			int expectedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
			
			for (java.lang.reflect.Field f : Types.class.getFields())
			{
				if (f.getType() == int.class && f.getModifiers() == expectedModifiers)
				{
					SQL_TYPES.put( f.getName(), f.getInt( null ) );	
				}
			}
			
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
			else if (tag.equals( TAG_CLASSES ))
			{
				loadClasses( e, classesSet );
			}
			else if (tag.equals( TAG_TABLE ))
			{
				XmlTable t = loadTable( e );
				tableMap.put( t.name, t );
			}
			else
			{
				unexpectedTag( element, e );
			}
		}
		
		for (XmlTable t : tableMap.values()) t.validate( t, tableMap );
		for (XmlTable t : tableMap.values()) t.instantiateFieldImplementation();
		for (XmlTable t : tableMap.values()) t.instantiateTableImplementation();
		for (XmlTable t : tableMap.values()) t.instantiateViewImplementation();
		for (XmlTable t : tableMap.values()) t.initializeTable();
		for (XmlTable t : tableMap.values()) t.relateFieldReferences();
		for (XmlTable t : tableMap.values()) t.finishTable();
		
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
		boolean enableMode = TypeBoolean.parse( getAttribute( logging, "enable-mode", "true", true ), "enable-mode" );
		
		if (!enableMode) 
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
		
		Rekord.setLogging( enableMode, actualIndices );
	}
	
	private void loadClasses( Element classes, Set<String> classSet )
	{
		String[] classNames = split( classes.getTextContent() );
		
		for (String cn : classNames)
		{
			classSet.add( cn );
		}
	}
	
	private XmlTable loadTable( Element tableElement )
	{
		XmlTable table = new XmlTable();
		table.name = getAttribute( tableElement, "name", null, true );
		table.keyNames = split( getAttribute( tableElement, "key", null, true ) );
		
		XmlIterator<Element> nodes = new XmlIterator<Element>( tableElement );
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			if (tag.equals( TAG_FIELDS ))
			{
				loadFields( e, table );
			}
			else if (tag.equals( TAG_VIEWS ))
			{
				loadViews( e, table );
			}
			else if (tag.equals( TAG_HISTORY ))
			{
				loadHistory( e, table );
			}
			else
			{
				unexpectedTag( tableElement, e );
			}
		}
		
		if (!table.viewMap.containsKey( "all" ))
		{
			List<String> fieldNames = new ArrayList<String>();
			for (XmlField fn : table.fieldMap.values()) {
				fieldNames.add( fn.name );
			}
			
			XmlView view = new XmlView();
			view.name = "all";
			view.fieldNames = fieldNames.toArray( new String[ fieldNames.size() ] );
			table.viewMap.put( view.name, view );
		}
		
		if (!table.viewMap.containsKey( "id" ))
		{
			XmlView view = new XmlView();
			view.name = "id";
			view.fieldNames = table.keyNames;
			table.viewMap.put( view.name, view );
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
			
			if (tag.equals( TAG_COLUMN ))
			{
				XmlColumn c = new XmlColumn();
				c.sqlType = SQL_TYPES.get( getAttribute( e, "type", null, true ) );
				c.in = getAttribute( e, "in", "?", true );
				c.out = getAttribute( e, "out", "?", true );
				c.defaultValueString = getAttribute( e, "default-value", null, false );
				field = c;
			}
			else if (tag.equals( TAG_FOREIGN_COLUMN ))
			{
				XmlForeignColumn c = new XmlForeignColumn();
				c.foreignTableName = getAttribute( e, "foreign-table", null, true );
				c.foreignColumnName = getAttribute( e, "foreign-column", null, true );
				c.sqlType = SQL_TYPES.get( getAttribute( e, "type", null, true ) );
				c.in = getAttribute( e, "in", "?", true );
                c.out = getAttribute( e, "out", "?", true );
                c.defaultValueString = getAttribute( e, "default-value", null, false );
				field = c;
			}
			else if (tag.equals( TAG_ONE_TO_ONE ))
			{
				XmlOneToOne c = new XmlOneToOne();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinViewName = getAttribute( e, "join-view", "all", true );
				field = c;
			}
			else if (tag.equals( TAG_MANY_TO_ONE ))
			{
				XmlManyToOne c = new XmlManyToOne();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinViewName = getAttribute( e, "join-view", "all", true );
				field = c;
			}
			else if (tag.equals( TAG_ONE_TO_MANY ))
			{
				XmlOneToMany c = new XmlOneToMany();
				c.joinTableName = getAttribute( e, "join-table", null, true );
				c.joinKeyNames = split( getAttribute( e, "join-key", null, true ) );
				c.joinViewName = getAttribute( e, "join-view", "all", true );
				c.fetchSizeString = getAttribute( e, "fetch-size", "128", true );
				c.cascadeDelete = TypeBoolean.parse( getAttribute( e, "cascade-delete", "false", true ), "cascade-delete of field " + fieldName + " in table " + table.name );
				field = c;
			}
			else
			{
				unexpectedTag( fields, e );
				return;
			}
			
			field.table = table;
			field.name = fieldName;
			field.flags = readFlags( e, field.name, table.name );
			table.fieldMap.put( field.name, field );
		}
	}
	
	private int readFlags(Element e, String fieldName, String tableName)
	{
		String lazy = getAttribute( e, "lazy", "false", true );
		String readOnly = getAttribute( e, "read-only", "false", true );
		String generated = getAttribute( e, "generated", "false", true );
		String nonNull = getAttribute( e, "non-null", "false", true );
		
		String messagePostfix = " of field " + fieldName + " in table " + tableName;
		
		return (
			(TypeBoolean.parse( lazy, "lazy" + messagePostfix ) ? Flags.LAZY : 0) |
			(TypeBoolean.parse( readOnly, "read-only" + messagePostfix ) ? Flags.READ_ONLY : 0) |
			(TypeBoolean.parse( generated, "generated" + messagePostfix ) ? Flags.GENERATED : 0) |
			(TypeBoolean.parse( nonNull, "non-null" + messagePostfix ) ? Flags.NON_NULL : 0)
		);
	}
	
	private void loadViews( Element views, XmlTable table )
	{
		XmlIterator<Element> nodes = new XmlIterator<Element>( views );
		
		for (Element e : nodes)
		{
			String tag = e.getTagName().toLowerCase();
			
			if (tag.equals( TAG_VIEW ))
			{
				XmlView v = new XmlView();
				v.name = getAttribute( e, "name", null, true );
				v.fieldNames = split( getAttribute( e, "fields", null, true ) );
				table.viewMap.put( v.name, v );
			}
			else
			{
				unexpectedTag( views, e );
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
	
	protected static Type<?> getType(int sqlType)
	{
	    switch (sqlType) {
	    case Types.TINYINT:
	        return TypeByte.INSTANCE;
	    case Types.SMALLINT:
	        return TypeShort.INSTANCE;
	    case Types.INTEGER:
	        return TypeInteger.INSTANCE;
	    case Types.BIGINT:
	        return TypeLong.INSTANCE;
	    case Types.CHAR:
	    case Types.LONGNVARCHAR:
	    case Types.LONGVARCHAR:
	    case Types.NCHAR:
	    case Types.NVARCHAR:
	    case Types.VARCHAR:
	        return TypeString.INSTANCE;
	    case Types.BINARY:
	    case Types.LONGVARBINARY:
	    case Types.VARBINARY:
	        return TypeByteArray.INSTANCE;
	    case Types.BIT:
	    case Types.BOOLEAN:
	        return TypeBoolean.INSTANCE;
	    case Types.DATE:
	        return TypeDate.INSTANCE;
	    case Types.TIME:
	        return TypeTime.INSTANCE;
	    case Types.TIMESTAMP:
	        return TypeTimestamp.INSTANCE;
	    case Types.DECIMAL:
	    case Types.NUMERIC:
	        return TypeDecimal.INSTANCE;
	    case Types.REAL:
	        return TypeFloat.INSTANCE;
        case Types.FLOAT:
	    case Types.DOUBLE:
	        return TypeDouble.INSTANCE;
	    case Types.JAVA_OBJECT:
	        return TypeObject.INSTANCE;
	    case Types.STRUCT:
	        return TypeStruct.INSTANCE;
	    case Types.ARRAY:
	        return TypeArray.INSTANCE;
	    case Types.REF:
	        return TypeRef.INSTANCE;
	    case Types.ROWID:
	        return TypeRowId.INSTANCE;
	    case Types.SQLXML:
	        return TypeXml.INSTANCE;
	    case Types.CLOB:
	        return TypeClob.INSTANCE;
	    case Types.BLOB:
	        return TypeBlob.INSTANCE;
	    default:
	        return TypeObject.INSTANCE;
	    }
	}
	
}
