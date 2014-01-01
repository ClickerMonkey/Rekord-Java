
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

	public DefaultTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		this.modelCache = new Map[ Rekord.getTableCount() ];
		
		for (int i = 0; i < Rekord.getTableCount(); i++)
		{
			if (Rekord.getTable( i ).is( Table.TRANSACTION_CACHED ))
			{
				this.modelCache[i] = new HashMap<Key, Model>();	
			}
		}
	}

	@Override
	public Connection getConnection()
	{
		return connection;
	}

	@Override
	public PreparedStatement prepare( String query ) throws SQLException
	{
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
	public void start()
	{
	    init( false, true );
	}

	@Override
	public void end( boolean commit )
	{
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
	public boolean isStarted()
	{
		return started;
	}

	@Override
	public void close()
	{
	    if (started)
	    {
	        throw new RuntimeException( "You cannot close a started transaction, you must end it first" );
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
	}

	@Override
	public <T extends Model> Map<Key, T> getCache( Table table )
	{
		return (Map<Key, T>)modelCache[table.getIndex()];
	}

	@Override
	public <T extends Model> T getCached( Table table, Key key )
	{
		Map<Key, Model> cache = modelCache[table.getIndex()];
		
		return (cache == null ? null : (T)cache.get( key ));
	}

	@Override
	public void cache( Model model )
	{
		Map<Key, Model> cache = modelCache[model.getTable().getIndex()];
		
		if (cache != null && model.hasKey())
		{
			cache.put( model.getKey(), model );
		}
	}

}
