
package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DefaultTransaction implements Transaction
{

	protected final Connection connection;
	protected final Map<String, PreparedStatement> statementCache;
	protected final Map<Key, Model>[] modelCache;
	protected boolean started;
	protected boolean closed;

	public DefaultTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		final int tableCount = Rekord.getTableCount();
		
		this.modelCache = new Map[ tableCount ];
		
		for (int i = 0; i < tableCount; i++)
		{
			if (Rekord.getTable( i ).is( Table.TRANSACTION_CACHED ))
			{
				this.modelCache[i] = new HashMap<Key, Model>();	
			}
		}
	}
	
	protected void checkOpen()
	{
		if (closed)
		{
			throw new RuntimeException( "You cannot perform this operation with a closed transaction" );
		}
	}
	
	protected void init( boolean autoCommit, boolean newStarted )
	{
	    try
	    {
	        connection.setAutoCommit( autoCommit );
	        started = newStarted;
	    }
	    catch (SQLException e)
	    {
	        started = false;
	        
	        throw new RuntimeException( e );
	    }
	}

	@Override
	public PreparedStatement prepare( String query ) throws SQLException
	{
		checkOpen();
		
		PreparedStatement stmt = statementCache.get( query );

		if (stmt == null)
		{
			stmt = connection.prepareStatement( query );
			statementCache.put( query, stmt );
		}
		else
		{
			stmt.clearParameters();
		}

		return stmt;
	}

	@Override
	public void start()
	{
		checkOpen();
	    init( false, true );
	}

	@Override
	public void end( boolean commit )
	{
		checkOpen();
		
	    try
        {
	        if (commit)
	        {
	            connection.commit();    
	        }
	        else
	        {
	            connection.rollback();
	        }
        }
        catch (SQLException e)
        {
            throw new RuntimeException( e );
        }
	    finally
	    {
	        init( true, false );
	    }
	}

	@Override
	public void close()
	{
	    if (started)
	    {
	        throw new RuntimeException( "You cannot close a started transaction, you must end it first" );
	    }
	    
	    if (closed)
	    {
	    	return;
	    }
	    
		for (PreparedStatement stmt : statementCache.values())
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
			    // log but ignore
				e.printStackTrace();
			}
		}

		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
		    // log but ignore
			e.printStackTrace();
		}
		
		closed = true;
	}

	@Override
	public boolean isStarted()
	{
		return started;
	}
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	public Connection getConnection()
	{
		return connection;
	}

	@Override
	public <T extends Model> Map<Key, T> getCache( Table table )
	{
		return (Map<Key, T>)modelCache[ table.getIndex() ];
	}

	@Override
	public <T extends Model> T getCached( Table table, Key key )
	{
		Map<Key, T> cache = getCache( table );
		
		return (cache == null ? null : cache.get( key ));
	}

	@Override
	public void cache( Model model )
	{
		Map<Key, Model> cache = getCache( model.getTable() );
		
		if (cache != null)
		{
			Key key = model.getKey();
			
			if (key.exists())
			{
				cache.put( key, model );	
			}
			else
			{
				Rekord.log( Logging.CACHING, "You cannot cache the following model without having a key: " + model );
			}
		}
	}

}
