
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
	protected final ModelCache[] committedCache;
	protected final ModelCache[] saveCache;
	protected final ModelCache[] deleteCache;
	protected boolean started;
	protected boolean closed;

	public DefaultTransaction( Connection connection )
	{
		this.connection = connection;
		this.statementCache = new HashMap<String, PreparedStatement>();

		final int tableCount = Rekord.getTableCount();
		
		this.committedCache = new ModelCache[ tableCount ];
		this.saveCache = new ModelCache[ tableCount ];
		this.deleteCache = new ModelCache[ tableCount ];
		
		for (int i = 0; i < tableCount; i++)
		{
		    Table table = Rekord.getTable( i );
		    
		    boolean cacheIt = table.is( Table.TRANSACTION_CACHED );
		    String cacheName = table.getName() + " (transaction-scope)";
		    
			this.committedCache[i] = new ModelCache( cacheIt ? new HashMap<Key, Model>() : null, cacheName + " committed" );
			this.saveCache[i] = new ModelCache( cacheIt ? new HashMap<Key, Model>() : null, cacheName + " uncommitted-save" );
			this.deleteCache[i] = new ModelCache( cacheIt ? new HashMap<Key, Model>() : null, cacheName + " uncommitted-purge" );
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
	            
	            int committedSaves = 0;
	            int committedDeletes = 0;
	            
	            for (int i = 0; i < committedCache.length; i++)
	            {
	                Map<Key, Model> appMap = Rekord.getCache( i );
                    Map<Key, Model> saveMap = saveCache[i].getMap();
                    Map<Key, Model> deleteMap = deleteCache[i].getMap();
                    Map<Key, Model> commitMap = committedCache[i].getMap();
	                
	                if (commitMap != null)
	                {
	                    committedSaves += saveMap.size();
	                    committedDeletes += deleteMap.size();
	                    
	                    if (appMap != null)
	                    {
	                        appMap.putAll( saveMap );
                            for (Key k : deleteMap.keySet()) appMap.remove( k );
	                    }
	                    
	                    commitMap.putAll( saveMap );
	                    for (Key k : deleteMap.keySet()) commitMap.remove( k );
	                    
	                    saveMap.clear();
	                    deleteMap.clear();
	                }
	            }
	            
	            Rekord.log( Logging.CACHING, "%d cache saves and %d cache deletions committed", committedSaves, committedDeletes );
	        }
	        else
	        {
	            connection.rollback();
	            
	            int rolledBackSaves = 0;
	            int rolledBackDeletions = 0;
	            
	            for (int i = 0; i < committedCache.length; i++)
                {
	                Map<Key, Model> saveMap = saveCache[i].getMap();
                    Map<Key, Model> deleteMap = deleteCache[i].getMap();
                    
                    if (saveMap != null)
                    {
                        rolledBackSaves += saveMap.size();
                        rolledBackDeletions += deleteMap.size();
                        
                        saveMap.clear();
                        deleteMap.clear();
                    }
                }
	            
	            Rekord.log( Logging.CACHING, "%d cache saves and %d cache deletions rolled-back", rolledBackSaves, rolledBackDeletions );
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
		return committedCache[ table.getIndex() ].getMap();
	}

	@Override
	public <T extends Model> T getCached( Table table, Key key )
	{
	    int i = table.getIndex();
	    T cached = null;
	    
	    if (started)
	    {
	        cached = saveCache[i].get( key );
	    }
	    
	    if (cached == null)
        {
            cached = committedCache[i].get( key );
            
            if (cached == null)
            {
                cached = Rekord.getCached( table, key );
            }
        }
	    
		return cached;
	}

	@Override
	public boolean cache( Table table, Model model )
	{
	    boolean cached = false;
	    int i = table.getIndex();
	    
	    if (started)
	    {
	        cached = saveCache[i].put( model );
	        
	        if (cached)
	        {
	            deleteCache[i].remove( model.getKey() );    
	        }
	    }
	    else
	    {
	        cached |= committedCache[i].put( model );
	        cached |= Rekord.cache( table, model );
	    }
	    
		return cached;
	}

	@Override
	public void purge( Table table, Model model )
	{
	    int i = table.getIndex();
	    
	    if (started)
	    {
	        if (deleteCache[i].put( model ))
	        {
	            saveCache[i].remove( model.getKey() );
	        }
	    }
	    else
	    {
	        committedCache[i].remove( model.getKey() );
	        Rekord.purge( table, model );    
	    }
	}
	
}
