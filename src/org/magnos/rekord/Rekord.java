
package org.magnos.rekord;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Query.group( COLUMN ); Query.group( expression ); Query.having( COLUMN,
 * CONDITION, value ); Query.having( expression );
 * 
 */
public class Rekord
{

	private static Table<?>[] tables = {};
	private static Map<String, Table<?>> tableMap = new HashMap<String, Table<?>>();
	
	private static Factory<Transaction> transactionFactory;
	private static ThreadLocal<Transaction> transactionLocal = new ThreadLocal<Transaction>();
	
	private static BitSet logging = new BitSet();
	private static PrintStream loggingStream = System.out;
	
	public static int newTable( Table<?> table )
	{
		int id = tables.length;
		tables = Arrays.copyOf( tables, id + 1 );
		tables[id] = table;
		tableMap.put( table.getName(), table );
		return id;
	}

	public static Table<?> getTable( int id )
	{
		return tables[id];
	}
	
	public static <T extends Model> Table<T> getTable( String name, Factory<T> factory )
	{
		Table<T> t = (Table<T>) tableMap.get( name );
		
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

		if (trans == null)
		{
			trans = transactionFactory.create();
			transactionLocal.set( trans );
		}

		return trans;
	}

	public static void close( Transaction transaction )
	{
		transaction.close();
		transactionLocal.set( null );
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

}
