
package org.magnos.rekord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.Select;
import org.magnos.rekord.query.SelectQuery;


public class Model implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	protected transient final Table table;
	protected transient final Key key;
	protected final Value<?>[] values;

	protected Model( Table table )
	{
		this.table = table;
		this.values = table.newValues( this );
		this.key = table.keyForModel( this );
	}
	
	public boolean hasChanged()
	{
	    for (Value<?> v : values)
	    {
	        if (v.hasChanged())
	        {
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	public boolean exists() throws SQLException
	{
	    return hasKey() && Select.find( this ).create().any();
	}

	public <T> void set( Field<T> field, T value )
	{
		valueOf( field ).set( this, value );
	}
	
	public <T> T get( Field<T> field )
	{
		return valueOf( field ).get( this );
	}

	public <T> Value<T> valueOf( Field<T> field )
	{
		return (Value<T>)values[field.getIndex()];
	}

	public boolean has( Field<?> field )
	{
		return values[field.getIndex()].hasValue();
	}

	public boolean save() throws SQLException
	{
		return hasKey() ? update() : insert();
	}
	
	public boolean save( SaveProfile saveProfile ) throws SQLException
	{
		Query<Model> saveQuery = saveProfile.newQuery( hasKey() );
		
		saveQuery.bind( this );
		
		return saveQuery.executeUpdate() > 0;
	}

	public boolean insert() throws SQLException
	{
		return table.getInsert().execute( this );
	}

	public boolean insert( SaveProfile saveProfile ) throws SQLException
	{
		Query<Model> insertQuery = saveProfile.getInsertTemplate().create();
		
		insertQuery.bind( this );
		
		return insertQuery.executeUpdate() > 0;
	}
	
	public boolean update() throws SQLException
	{
		return table.getUpdate().execute( this );
	}
	
	public boolean update( SaveProfile saveProfile ) throws SQLException
	{
		Query<Model> updateQuery = saveProfile.getUpdateTemplate().create();
		
		updateQuery.bind( this );
		
		return updateQuery.executeUpdate() > 0;
	}

	public boolean delete() throws SQLException
	{
		return hasKey() ? table.getDelete().execute( this ) : false;
	}

	public boolean load( LoadProfile loadProfile, boolean overwriteExisting ) throws SQLException
	{
		if (!hasKey())
		{
			return false;
		}

		SelectQuery<Model> select = new SelectQuery<Model>( this );

		for (Field<?> f : loadProfile.getFields())
		{
			if (!valueOf( f ).hasValue() && f.isSelectable())
			{
			    select.select( f, f.getSelectExpression( loadProfile.getFieldLoad( f ) ) );
			}
		}

		select.setLoadProfile( loadProfile );
		
        Query<Model> query = select.create();
		
		if (select.hasSelection())
		{
		    query.bind( this );
		    
			ResultSet results = query.getResults();

			try
			{
				if (results.next())
				{
				    if (overwriteExisting)
				    {
				        query.populate( results, this );    
				    }
				    else
				    {
				        query.merge( results, this );
				    }
				}
				else
				{
					return false;
				}
			}
			finally
			{
				results.close();
			}
			
			Rekord.log( Logging.LOADING, "loaded profile %s for %s", loadProfile.getName(), this );
		}
		else
		{
			Rekord.log( Logging.LOADING, "loading profile %s had no affect for %s", loadProfile.getName(), this );
		}

		query.postSelect( this );
		
		return true;
	}
	
	public <T extends Model> Map<HistoryKey<T>, T> getHistory() throws SQLException
	{
		if (!table.hasHistory() || !hasKey())
		{
			return Collections.emptyMap();
		}
		
		LinkedHashMap<HistoryKey<T>, T> resultMap = new LinkedHashMap<HistoryKey<T>, T>();

		HistoryTable history = table.getHistory();
		String historyKey = history.getHistoryKey();
		String historyTimestamp = history.getHistoryTimestamp();
		
		Query<Model> query = history.getQuery().create();
		query.bind( this );
		
		ResultSet results = query.getResults();
		
		try
		{
			while (results.next())
			{
				T model = table.newModel();
				
				query.populate( results, model );
				
				HistoryKey<T> key = new HistoryKey<T>();
				
				if (historyKey != null)
				{
					key.setHistoryId( results.getLong( historyKey ) );	
				}
				
				if (historyTimestamp != null)
				{
					key.setHistoryTimestamp( results.getTimestamp( historyTimestamp ) );
				}
				
				key.setModel( model );
				
				resultMap.put( key, model );
			}
		}
		finally
		{
			results.close();
		}
		
		return resultMap;
	}

	public Key getKey()
	{
		return key;
	}

	public boolean hasKey()
	{
		return key.exists();
	}

	public Table getTable()
	{
		return table;
	}

	public Value<?>[] getValues()
	{
		return values;
	}
	
	public Value<?>[] getValues(Field<?> ... fields)
	{
		Value<?>[] customValues = new Value[ fields.length ];
		
		for (int i = 0; i < fields.length; i++)
		{
			customValues[i] = values[ fields[i].getIndex() ];
		}
		
		return customValues;
	}

	public int hashCode()
	{
		return key.hashCode();
	}

	public boolean equals( Object o )
	{
		if (o == null || !(o instanceof Model))
		{
			return false;
		}

		Model m = (Model)o;

		return key.equals( m.key );
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( table.getName() );
		sb.append( "{" );
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].hasValue())
			{
				if (i > 0)
				{
					sb.append( ", " );
				}
				
				sb.append( values[i] );	
			}
		}
		sb.append( "}" );
		return sb.toString();
	}

	private void writeObject( ObjectOutputStream out ) throws IOException
	{
		for (int i = 0; i < values.length; i++)
		{
			values[i].serialize( out );
		}
	}

	private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
	{
		for (int i = 0; i < values.length; i++)
		{
			values[i].deserialize( in );
		}
	}

	protected Object readResolveModel()
	{
		Object resolved = this;
		Transaction trans = Rekord.getTransaction();

		if (trans != null && trans.isStarted() && hasKey())
		{
			Model cached = trans.getCached( table, key );

			if (cached != null)
			{
				resolved = cached;
			}
		}

		return resolved;
	}

}
