
package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class AbstractTransaction implements Transaction
{

	protected final Connection connection;
	protected final Map<String, PreparedStatement> statementCache;
	protected final Map<Key, Model>[] modelCache;
	protected boolean started;

	public AbstractTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		this.modelCache = new Map[ Rekord.getTableCount() ];
		
		for (int i = 0; i < Rekord.getTableCount(); i++)
		{
			this.modelCache[i] = new HashMap<Key, Model>();
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
		return (Map<Key, T>)modelCache[table.id()];
	}

	@Override
	public <T extends Model> T getCached( Table table, Key key )
	{
		return (T)modelCache[table.id()].get( key );
	}

	@Override
	public void cache( Model model )
	{
		modelCache[model.getTable().id()].put( model.getKey(), model );
	}

}
