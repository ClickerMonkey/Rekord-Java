
package org.magnos.rekord;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.magnos.rekord.util.ModelCache;
import org.w3c.dom.Document;

public class Rekord
{

	private static Table[] tables = {};
	private static Map<String, Table> tableMap = new HashMap<String, Table>();
	private static ModelCache[] tableCache = {};
	
	private static Factory<Transaction> transactionFactory;
	private static ThreadLocal<Transaction> transactionLocal = new ThreadLocal<Transaction>();
	
	private static HashMap<Class<?>, Type<?>> typeClassMap = new HashMap<Class<?>, Type<?>>();
	private static HashMap<String, Type<?>> typeNameMap = new HashMap<String, Type<?>>();
	
	private static BitSet logging = new BitSet();
	private static PrintStream loggingStream = System.out;
	
	public static int newTable( Table table )
	{
		int index = tables.length;
		
		tables = Arrays.copyOf( tables, index + 1 );
		tables[index] = table;
		
		tableCache = Arrays.copyOf( tableCache, index + 1 );
		tableCache[index] = new ModelCache( table.is( Table.APPLICATION_CACHED ) ? new ConcurrentHashMap<Key, Model>() : null, table.getName() + " (application-scope)" );
		
		tableMap.put( table.getName(), table );
		
		return index;
	}

	public static Table getTable( int index )
	{
		return tables[index];
	}
	
	public static Table getTable( String name, Factory<? extends Model> factory )
	{
		Table t = tableMap.get( name );
		
		if (t != null)
		{
			t.setFactory( factory );
		}
		
		return t;
	}
	
	public static Map<Key, Model> getCache(int tableIndex)
	{
	    return tableCache[ tableIndex ].getMap();
	}
	
	public static <T extends Model> Map<Key, T> getCache(Table table)
	{
		return tableCache[ table.getIndex() ].getMap();
	}
	
	public static <T extends Model> T getCached(Table table, Key key)
	{
		return tableCache[ table.getIndex() ].get( key );
	}
	
	public static boolean cache(Table table, Model model)
	{
		return tableCache[ table.getIndex() ].put( model );
	}

	public static void purge(Table table, Model model)
	{
	    tableCache[ table.getIndex() ].remove( model.getKey() );
	}
	
	public static int getTableCount()
	{
		return tables.length;
	}

	public static Transaction getTransaction()
	{
		Transaction trans = transactionLocal.get();

		if (trans == null || trans.isClosed())
		{
			trans = transactionFactory.create();
			transactionLocal.set( trans );
		}

		return trans;
	}

	public static void setTransactionFactory( Factory<Transaction> factory )
	{
		transactionFactory = factory;
	}
	
	public static boolean isLogging(int id)
	{
		return logging.get( id );
	}
	
	public static void setLogging( boolean enabled, int ... loggings )
	{
		for (int id : loggings)
		{
			logging.set( id, enabled );
		}		
	}
	
	public static void setLoggingStream(PrintStream stream)
	{
		loggingStream = stream;
	}
	
	public static void log( int id, String message )
	{
		if (isLogging( id ))
		{
			log( message );
		}
	}
	
	public static void log( int id, String format, Object ... arguments)
	{
		if (isLogging( id ))
		{
			log( format, arguments );
		}
	}
	
	public static void log( String message )
	{
		loggingStream.println( message );
	}
	
	public static void log( String format, Object ... arguments )
	{
		loggingStream.format( format + "\n", arguments );
	}

	static
	{
	    addType( TypeByte.INSTANCE, Byte.class, byte.class );
	    addType( TypeShort.INSTANCE, Short.class, short.class );
	    addType( TypeInteger.INSTANCE, Integer.class, int.class );
	    addType( TypeLong.INSTANCE, Long.class, long.class );
	    addType( TypeString.INSTANCE, Character.class, char.class, String.class );
	    addType( TypeByteArray.INSTANCE, byte[].class );
	    addType( TypeBoolean.INSTANCE, Boolean.class, boolean.class );
	    addType( TypeDate.INSTANCE, Date.class, java.sql.Date.class );
	    addType( TypeTime.INSTANCE, Time.class );
	    addType( TypeTimestamp.INSTANCE, Timestamp.class );
	    addType( TypeDecimal.INSTANCE, BigDecimal.class );
	    addType( TypeDouble.INSTANCE, Double.class, double.class );
	    addType( TypeFloat.INSTANCE, Float.class, float.class );
	    addType( TypeArray.INSTANCE, Array.class );
	    addType( TypeRowId.INSTANCE, RowId.class );
	    addType( TypeRef.INSTANCE, Ref.class );
	    addType( TypeXml.INSTANCE, Document.class );
	    addType( TypeClob.INSTANCE, Clob.class );
	    addType( TypeBlob.INSTANCE, Blob.class );

        addType( TypeByte.INSTANCE, "tinyint" );
        addType( TypeShort.INSTANCE, "smallint" );
        addType( TypeLong.INSTANCE, "bigint" );
	    addType( TypeString.INSTANCE, "text", "varchar", "varying character", "longnvarchar", "nchar", "nvarchar" );
	    addType( TypeByteArray.INSTANCE, "binary", "varbinary", "longvarbinary" );
	    addType( TypeBoolean.INSTANCE, "bit" );
        addType( TypeDecimal.INSTANCE, "decimal", "numeric" );
        addType( TypeFloat.INSTANCE, "real" );	    
	    addType( TypeXml.INSTANCE, "xml" );
	}
    
    public static void addType( Type<?> type, Class<?> ... classes )
    {
        for (Class<?> c : classes)
        {
            typeClassMap.put( c, type );
            typeNameMap.put( c.getSimpleName().toLowerCase(), type );
        }
    }
    
    public static void addType( Type<?> type, String ... names )
    {
        for (String n : names)
        {
            typeNameMap.put( n.toLowerCase(), type );
        }
    }
	
	public static Type<?> getType(int sqlType)
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
    
    public static Type<?> getType( Class<?> clazz )
    {
        Type<?> t = typeClassMap.get( clazz );
        
        return (t == null ? TypeObject.INSTANCE : t);
    }
    
    public static Type<?> getType( String name )
    {
        Type<?> t = typeNameMap.get( name.toLowerCase() );
        
        return (t == null ? TypeObject.INSTANCE : t);
    }
    
    public static <T> Type<T> getTypeForObject( T o )
    {
        return (Type<T>)(o == null ? TypeObject.INSTANCE : getType( o.getClass() ));
    }

}
