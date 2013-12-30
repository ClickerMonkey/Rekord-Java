
package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class AbstractTransaction implements Transaction
{

	protected Connection connection;
	protected Map<String, PreparedStatement> statementCache;
	protected boolean started;
	protected Map<Key, Model>[] modelCache;

	public AbstractTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		this.modelCache = new Map[Rekord.getTableCount()];
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

	@Override
	public void start()
	{
		try
		{
			connection.setAutoCommit( false );

			started = true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void end( boolean commit )
	{
		if (commit)
		{
			commit();
		}
		else
		{
			rollback();
		}
	}

	@Override
	public void commit()
	{
		try
		{
			if (!connection.getAutoCommit())
			{
				connection.commit();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void rollback()
	{
		try
		{
			if (!connection.getAutoCommit())
			{
				connection.rollback();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
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
		for (PreparedStatement stmt : statementCache.values())
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		started = false;
	}

	@Override
	public <T extends Model> Map<Key, T> getCache( Table<T> table )
	{
		return (Map<Key, T>)modelCache[table.id()];
	}

	@Override
	public <T extends Model> T getCached( Table<T> table, Key key )
	{
		return (T)modelCache[table.id()].get( key );
	}

	@Override
	public <T extends Model> void cache( T model )
	{
		modelCache[model.getTable().id()].put( model.getKey(), model );
	}

}
