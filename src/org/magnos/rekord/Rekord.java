
package org.magnos.rekord;

import java.io.PrintStream;
import java.sql.Types;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

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


/**
 * 
 * Query.group( COLUMN ); Query.group( expression ); Query.having( COLUMN,
 * CONDITION, value ); Query.having( expression );
 * 
 */
public class Rekord
{

	private static Table[] tables = {};
	private static Map<String, Table> tableMap = new HashMap<String, Table>();
	
	private static Factory<Transaction> transactionFactory;
	private static ThreadLocal<Transaction> transactionLocal = new ThreadLocal<Transaction>();
	
	private static BitSet logging = new BitSet();
	private static PrintStream loggingStream = System.out;
	
	public static int newTable( Table table )
	{
		int index = tables.length;
		tables = Arrays.copyOf( tables, index + 1 );
		tables[index] = table;
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
		loggingStream.append( message );
	}
	
	public static void log( String format, Object ... arguments )
	{
		loggingStream.format( format + "\n", arguments );
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

}
