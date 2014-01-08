
package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.magnos.rekord.util.ModelCache;


public class DefaultTransaction implements Transaction
{

	protected final Connection connection;
	protected final Map<String, PreparedStatement> statementCache;
	protected final ModelCache[] modelCache;
	protected boolean started;
	protected boolean closed;

	public DefaultTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		final int tableCount = Rekord.getTableCount();
		
		this.modelCache = new ModelCache[ tableCount ];
		
		for (int i = 0; i < tableCount; i++)
		{
		    Table table = Rekord.getTable( i );
		    
			this.modelCache[i] = new ModelCache( table.is( Table.TRANSACTION_CACHED ) ? new HashMap<Key, Model>() : null, table.getName() + " (transaction-scope)" );
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
		return modelCache[ table.getIndex() ].getMap();
	}

	@Override
	public <T extends Model> T getCached( Table table, Key key )
	{
		T cached = Rekord.getCached( table, key );
		
		if (cached == null)
		{
			cached = modelCache[ table.getIndex() ].get( key );
		}
		
		return cached;
	}

	@Override
	public boolean cache( Model model )
	{
		boolean cached = Rekord.cache( model );
		
		if (!cached)
		{
			cached = modelCache[ model.getTable().getIndex() ].put( model );
		}
		
		return cached;
	}

	@Override
	public void purge( Model model )
	{
	    Rekord.purge( model );
	    
	    modelCache[ model.getTable().getIndex() ].remove( model.getKey() );
	}
	
}
