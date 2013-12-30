
package org.magnos.rekord.xml;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.magnos.rekord.Column;
import org.magnos.rekord.DefaultTransactionFactory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Flags;
import org.magnos.rekord.ForeignColumn;
import org.magnos.rekord.Formula;
import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.Logging;
import org.magnos.rekord.ManyToOne;
import org.magnos.rekord.OneToMany;
import org.magnos.rekord.OneToOne;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;
import org.magnos.rekord.util.ArrayUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@SuppressWarnings ("rawtypes" )
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
    private static final String TAG_FORMULA = "formula";
	
	private static final Map<String, Integer> SQL_TYPES = new HashMap<String, Integer>();
	private static final Map<String, Integer> LOGGINGS = new HashMap<String, Integer>();
	private static final Set<String> TRUES = new HashSet<String>( Arrays.asList( "1", "t", "true", "y", "ya", "yes", "yessums" ) );
	
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
		
		for (XmlTable t : tableMap.values())
		{
			t.validate( tableMap );
		}
		
		for (XmlTable t : tableMap.values())
		{
			t.build( tableMap );
		}
		
		for (XmlTable t : tableMap.values())
		{
			t.postbuild( tableMap );
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
		boolean enableMode = TRUES.contains( getAttribute( logging, "enable-mode", "true" ) );
		
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
		table.name = getAttribute( tableElement, "name", null );
		table.keyNames = split( getAttribute( tableElement, "key", null ) );
		
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
			
			if (tag.equals( TAG_COLUMN ))
			{
				XmlColumn c = new XmlColumn();
				c.type = SQL_TYPES.get( getAttribute( e, "type", null ) );
				field = c;
			}
			else if (tag.equals( TAG_FOREIGN_COLUMN ))
			{
				XmlForeignColumn c = new XmlForeignColumn();
				c.foreignTableName = getAttribute( e, "foreign-table", null );
				c.foreignColumnName = getAttribute( e, "foreign-column", null );
				c.type = SQL_TYPES.get( getAttribute( e, "type", null ) );
				field = c;
			}
			else if (tag.equals( TAG_ONE_TO_ONE ))
			{
				XmlOneToOne c = new XmlOneToOne();
				c.joinTableName = getAttribute( e, "join-table", null );
				c.joinKeyNames = split( getAttribute( e, "join-key", null ) );
				c.joinViewName = getAttribute( e, "join-view", "all" );
				field = c;
			}
			else if (tag.equals( TAG_MANY_TO_ONE ))
			{
				XmlManyToOne c = new XmlManyToOne();
				c.joinTableName = getAttribute( e, "join-table", null );
				c.joinKeyNames = split( getAttribute( e, "join-key", null ) );
				c.joinViewName = getAttribute( e, "join-view", "all" );
				field = c;
			}
			else if (tag.equals( TAG_ONE_TO_MANY ))
			{
				XmlOneToMany c = new XmlOneToMany();
				c.joinTableName = getAttribute( e, "join-table", null );
				c.joinKeyNames = split( getAttribute( e, "join-key", null ) );
				c.joinViewName = getAttribute( e, "join-view", "all" );
				c.fetchSizeString = getAttribute( e, "fetch-size", "128" );
				field = c;
			}
			else if (tag.equals( TAG_FORMULA ))
			{
			    XmlFormula c = new XmlFormula();
			    c.alias = getAttribute( e, "alias", null );
			    c.equation = getAttribute( e, "equation", null );
			    field = c;
			}
			else
			{
				unexpectedTag( fields, e );
				return;
			}
			
			field.table = table;
			field.name = getAttribute( e, "name", null );
			field.flags = readFlags( e );
			table.fieldMap.put( field.name, field );
		}
	}
	
	private int readFlags(Element e)
	{
		String lazy = getAttribute( e, "lazy", "false" ).toLowerCase();
		String readOnly = getAttribute( e, "read-only", "false" ).toLowerCase();
		String generated = getAttribute( e, "generated", "false" ).toLowerCase();
		
		return (
			(TRUES.contains( lazy ) ? Flags.LAZY : 0) |
			(TRUES.contains( readOnly ) ? Flags.READ_ONLY : 0) |
			(TRUES.contains( generated ) ? Flags.GENERATED : 0)
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
				v.name = getAttribute( e, "name", null );
				v.fieldNames = split( getAttribute( e, "fields", null ) );
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
		table.historyTable = getAttribute( history, "table", null );
		table.historyKey = getAttribute( history, "key", "" );
		table.historyTimestamp = getAttribute( history, "timestamp", "" );
		table.historyColumnNames = split( getAttribute( history, "columns", null ) );
	}
	
	private void unexpectedTag( Element parent, Element child )
	{
		throw new RuntimeException( "Unexpected tag '" + child.getTagName() + "' in element '" + parent.getTagName() + "'");
	}
	
	private String getAttribute(Element e, String name, String defaultValue)
	{
		String value = defaultValue;
		
		if (e.hasAttribute( name ))
		{
			value = e.getAttribute( name );
		}
		
		if (value == null)
		{
			throw new NullPointerException( "Attribute " + name + " is required and missing from " + e.getTagName() );
		}
		
		return value;
	}
	
	private String[] split(String x)
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
	
	private <T> T[] valuesFrom(Map<String, T> map, String[] keys, T ... placeHolder)
	{
		T[] values = Arrays.copyOf( placeHolder, keys.length );
		
		for (int i = 0; i < keys.length; i++)
		{
			values[i] = map.get( keys[i] );
		}
		
		return values;
	}
	
	private XmlField[] getFields(XmlTable table, String[] names, String format, Object ... arguments)
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
	
	private <F extends Field<?>> F[] getFields(XmlField[] xmlFields, F ... placeHolder)
	{
		F[] fieldArray = Arrays.copyOf( placeHolder, xmlFields.length );
		
		for (int i = 0; i < xmlFields.length; i++)
		{
			fieldArray[i] = (F)xmlFields[i].field;
		}
		
		return fieldArray;
	}
	
	class XmlTable
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
		
		void validate(Map<String, XmlTable> tableMap)
		{
			keys = getFields( this, keyNames, "key value %s on table %s was not specified in fields", name );
			
			for (XmlField f : fieldMap.values())
			{
				f.validate( this, tableMap );
			}
			
			for (XmlView v : viewMap.values())
			{
				v.validate( this, tableMap );
			}
			
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
				historyColumns = getFields( this, historyColumnNames, "history column %s for table %s was not found in the fields of the table", name );	
			}
		}
		
		void build(Map<String, XmlTable> tableMap) throws Exception
		{
			for (XmlField f : fieldMap.values())
			{
				f.build( this, tableMap );
			}
			
			keyColumns = getFields( keys );
			table = new Table( name, keyColumns );
			
			for (XmlView v : viewMap.values())
			{
				v.build( this, tableMap );
			}
		}
		
		void postbuild(Map<String, XmlTable> tableMap)
		{
			if (historyColumns != null)
			{
				Column<?>[] columns = getFields( historyColumns );
				
				table.setHistory( new HistoryTable( historyTable, historyKey, historyTimestamp, columns ) );
			}
			
			Collection<XmlField> fc = fieldMap.values();
			table.setFields( getFields( fc.toArray( new XmlField[fc.size()] ) ) );
			
			for (XmlView v : viewMap.values())
			{
				v.postbuild( this, tableMap );
			}
			
			for (XmlField f : fieldMap.values())
			{
				f.postbuild( this, tableMap );
			}
			
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
	
	abstract class XmlField
	{
		XmlTable table;
		XmlTable relatedTable;
		String name;
		int flags;
		
		Field<?> field;
		
		abstract void validate(XmlTable table, Map<String, XmlTable> tableMap);
		abstract void build(XmlTable table, Map<String, XmlTable> tableMap);
		abstract void postbuild(XmlTable table, Map<String, XmlTable> tableMap);
	}
    
    class XmlColumn extends XmlField
    {
        Integer type;

        void validate(XmlTable table, Map<String, XmlTable> tableMap)
        {
            if (type == null)
            {
                throw new RuntimeException( "unknown type specified for " + name + " on table " + table.name );
            }
        }
        
        void build(XmlTable table, Map<String, XmlTable> tableMap)
        {
            field = new Column( name, type, flags );
        }
        
        void postbuild(XmlTable table, Map<String, XmlTable> tableMap)
        {
            
        }
    }
    
    class XmlFormula extends XmlField
    {
        String equation;
        String alias;

        void validate(XmlTable table, Map<String, XmlTable> tableMap)
        {
        }
        
        void build(XmlTable table, Map<String, XmlTable> tableMap)
        {
            field = new Formula( name, equation, alias, flags );
        }
        
        void postbuild(XmlTable table, Map<String, XmlTable> tableMap)
        {
            
        }
    }
	
	class XmlForeignColumn extends XmlColumn
	{
		String foreignTableName;
		String foreignColumnName;
		
		XmlTable foreignTable;
		XmlField foreignColumn;
		
		void validate(XmlTable table, Map<String, XmlTable> tableMap)
		{
			super.validate( table, tableMap );
			
			foreignTable = tableMap.get( foreignTableName );
			
			if (foreignTable == null)
			{
				throw new RuntimeException( "foreign-table " + foreignTableName + " specified for field " + name + " was not found" );
			}
			
			foreignColumn = foreignTable.fieldMap.get( foreignColumnName );
			
			if (foreignColumn == null)
			{
				throw new RuntimeException( "foreign-column " + foreignColumnName + " specified for field " + name + " was not found" );
			}
			
			relatedTable = foreignTable;
		}
		
		void build(XmlTable table, Map<String, XmlTable> tableMap)
		{
			field = new ForeignColumn( name, type );
		}
		
		void postbuild(XmlTable table, Map<String, XmlTable> tableMap)
		{
			ForeignColumn<Object> fc = (ForeignColumn<Object>)field;
			fc.setForeignColumn( (Column<Object>)foreignColumn.field );
		}
	}
	
	class XmlOneToOne extends XmlField
	{
		String joinTableName;
		String joinViewName;
		String[] joinKeyNames;
		
		XmlTable joinTable;
		XmlField[] joinKey;
		XmlView view;
		
		void validate(XmlTable table, Map<String, XmlTable> tableMap)
		{
			joinTable = tableMap.get( joinTableName );
			
			if (joinTable == null)
			{
				throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
			}
			
			joinKey = getFields( table, joinKeyNames, "join-key value %s specified for field %s was not found", name );

			view = joinTable.viewMap.get( joinViewName );
			
			if (view == null)
			{
				throw new RuntimeException( "join-view " + joinViewName + " specified for field " + name + " was not found" );
			}
			
			relatedTable = joinTable;
		}

		void build( XmlTable table, Map<String, XmlTable> tableMap )
		{
			field = new OneToOne( name, flags );
		}

		void postbuild( XmlTable table, Map<String, XmlTable> tableMap )
		{
			OneToOne f = (OneToOne)field;
			ForeignColumn<?>[] fcs = getFields( joinKey );
			f.setJoin( joinTable.table, view.view, fcs );
		}
	}
	

	class XmlManyToOne extends XmlField
	{
		String joinTableName;
		String joinViewName;
		String[] joinKeyNames;
		
		XmlTable joinTable;
		XmlField[] joinKey;
		XmlView view;
		
		void validate(XmlTable table, Map<String, XmlTable> tableMap)
		{
			joinTable = tableMap.get( joinTableName );
			
			if (joinTable == null)
			{
				throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
			}
			
			joinKey = getFields( table, joinKeyNames, "join-key value %s specified for field %s was not found", name );

			view = joinTable.viewMap.get( joinViewName );
			
			if (view == null)
			{
				throw new RuntimeException( "join-view " + joinViewName + " specified for field " + name + " was not found" );
			}
			
			relatedTable = joinTable;
		}

		void build( XmlTable table, Map<String, XmlTable> tableMap )
		{
			field = new ManyToOne( name, flags );
		}

		void postbuild( XmlTable table, Map<String, XmlTable> tableMap )
		{
			ManyToOne f = (ManyToOne)field;
			ForeignColumn<?>[] fcs = getFields( joinKey );
			f.setJoin( joinTable.table, view.view, fcs );
		}
	}
	
	class XmlOneToMany extends XmlField
	{
		String joinTableName;
		String joinViewName;
		String[] joinKeyNames;
		String fetchSizeString;
		
		int fetchSize;
		XmlTable joinTable;
		XmlField[] joinKey;
		XmlView view;
		
		void validate(XmlTable table, Map<String, XmlTable> tableMap)
		{
			joinTable = tableMap.get( joinTableName );
			
			if (joinTable == null)
			{
				throw new RuntimeException( "join-table " + joinTableName + " specified for field " + name + " was not found" );
			}

			joinKey = getFields( joinTable, joinKeyNames, "join-key value %s specified for field %s was not found", name );
			
			view = joinTable.viewMap.get( joinViewName );
			
			if (view == null)
			{
				throw new RuntimeException( "join-view " + joinViewName + " specified for field " + name + " was not found" );
			}
			
			relatedTable = joinTable;
			
			try
			{
				fetchSize = Integer.parseInt( fetchSizeString );
			}
			catch (NumberFormatException e)
			{
				throw new RuntimeException( "fetch-size is not a valid number: " + fetchSizeString, e );
			}
			
			if (fetchSize <= 0)
			{
				throw new RuntimeException( "fetch-size must be a positive number greater than zero: " + fetchSize );
			}
		}

		void build( XmlTable table, Map<String, XmlTable> tableMap )
		{
			field = new OneToMany( name, flags, fetchSize );
		}

		void postbuild( XmlTable table, Map<String, XmlTable> tableMap )
		{
			OneToMany f = (OneToMany)field;
			ForeignColumn<?>[] fcs = getFields( joinKey );
			f.setJoin( joinTable.table, view.view, fcs );
		}
	}
	
	static final Pattern VIEW_NAME_PATTERN = Pattern.compile( "^([^\\[]+)\\[([^\\]]+)\\]$" );
	
	class XmlView
	{
		String name;
		String[] fieldNames;
		
		XmlField[] fields;
		View view;
		Map<XmlField, XmlView> fieldViews = new LinkedHashMap<XmlField, XmlView>();
		
		void validate(XmlTable table, Map<String, XmlTable> tableMap)
		{
			fields = new XmlField[ fieldNames.length ];
			
			for (int i = 0; i < fieldNames.length; i++)
			{
				String fn = fieldNames[i];
				String vn = null;
				
				Matcher matcher = VIEW_NAME_PATTERN.matcher( fn );
				
				if (matcher.matches())
				{
					fn = matcher.group( 1 );
					vn = matcher.group( 2 );
				}
				
				XmlField f = table.fieldMap.get( fn );
				
				if (f == null)
				{
					throw new RuntimeException( "field " + fn + " for view " + name + " was not found on table " + table.name);
				}
				
				fields[i] = f;
				
				if (vn != null)
				{
					fieldViews.put( f, f.relatedTable.viewMap.get( vn ) );
				}
			}
		}
		
		void build( XmlTable table, Map<String, XmlTable> tableMap )
		{
			view = new View( name, getFields( fields ) );
		}
		
		void postbuild( XmlTable table, Map<String, XmlTable> tableMap )
		{
			for (Entry<XmlField, XmlView> e :  fieldViews.entrySet())
			{
				view.add( e.getKey().field, e.getValue().view );
			}
		}
	}
	
}
