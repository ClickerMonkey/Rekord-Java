
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.magnos.rekord.Column;
import org.magnos.rekord.Field;
import org.magnos.rekord.ForeignColumn;
import org.magnos.rekord.Key;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Order;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.View;
import org.magnos.rekord.condition.AndCondition;
import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.condition.OperatorCondition;
import org.magnos.rekord.util.SqlUtil;


public class SelectQuery<M extends Model>
{

	protected Table<M> table;
	protected String from;
	protected Condition condition;
	protected StringBuilder selecting;
	protected StringBuilder ordering;
	protected List<Field<?>> selectFields;
	protected View view;
	protected Long offset;
	protected Long limit;

	public SelectQuery( M model )
	{
		this( (Table<M>)model.getTable() );
		this.byKey( model.getKey() );
	}
	
	public SelectQuery( Table<M> table )
	{
		this.table = table;
		this.from = table.getName();
		this.selecting = new StringBuilder();
		this.ordering = new StringBuilder();
		this.selectFields = new ArrayList<Field<?>>();
	}
	
	public SelectQuery<M> from( String from )
	{
		this.from = from;
		
		return this;
	}

	public SelectQuery<M> select( Table<M> table )
	{
		for (Field<?> f : table.getFields())
		{
			f.prepareSelect( this );
		}

		return this;
	}

	public SelectQuery<M> select( View view )
	{
		for (Field<?> f : view.getFields())
		{
			f.prepareSelect( this );
		}
		
		this.view = view;

		return this;
	}

	public SelectQuery<M> addPostSelectField( Field<?> field )
	{
		selectFields.add( field );
		
		return this;
	}
	
	public SelectQuery<M> select( String selector )
	{
		if (selector != null && selector.length() > 0)
		{
			if (selecting.length() > 0)
			{
				selecting.append( ", " );
			}
			
			selecting.append( selector );	
		}
		
		return this;
	}
	
	public SelectQuery<M> select( Field<?> field, String selector )
	{
		select( selector );

		selectFields.add( field );

		return this;
	}
	
	public SelectQuery<M> select( Key key )
	{
		for (int i = 0; i < key.size(); i++)
		{
			Column<?> c = key.fieldAt( i );
			
			select( c, "?" );
		}
		
		return this;
	}

	public SelectQuery<M> where( Condition condition )
	{
		this.condition = condition;

		return this;
	}
	
	public SelectQuery<M> orderBy( Column<?> column, Order order )
	{
		return orderBy( column.getName(), order );
	}
	
	public SelectQuery<M> orderBy( String expression, Order order )
	{
		if (ordering.length() > 0)
		{
			ordering.append( ", " );
		}
		
		ordering.append( expression );
		ordering.append( " " );
		ordering.append( order );
		
		return this;
	}
	
	public SelectQuery<M> limit( Long limit )
	{
		this.limit = limit;
		
		return this;
	}
	
	public SelectQuery<M> offset( Long offset )
	{
		this.offset = offset;
		
		return this;
	}
	
	public SelectQuery<M> clear()
	{
		this.limit = null;
		this.offset = null;
		this.condition = null;
		this.selectFields.clear();
		this.selecting.setLength( 0 );
		this.ordering.setLength( 0 );
		
		return this;
	}
	
	public boolean hasSelection()
	{
		return selecting.length() > 0;
	}
	
	public String getSelecting()
	{
		return selecting.toString();
	}
	
	public View getView()
	{
		return view;
	}
	
	public void setView( View view )
	{
		this.view = view;
	}

	public <T> SelectQuery<M> by(Column<T> column, T value)
	{
		return where( new OperatorCondition( column, Operator.EQ, value ) );
	}
	
	public SelectQuery<M> byKey(Key key)
	{
		final Condition[] conditions = new Condition[ key.size() ];
		
		for (int i = 0; i < key.size(); i++)
		{
			conditions[i] = new OperatorCondition( key.fieldAt( i ), Operator.EQ, key.valueAt( i ) );
		}
		
		return where( new AndCondition( conditions ) );
	}
	
	public SelectQuery<M> byForeignKey(Key key)
	{
		final Condition[] conditions = new Condition[ key.size() ];
		
		for (int i = 0; i < key.size(); i++)
		{
			Column<?> foreign = ((ForeignColumn<?>)key.fieldAt( i )).getForeignColumn();
			
			conditions[i] = new OperatorCondition( foreign, Operator.EQ, key.valueAt( i ) );
		}
		
		return where( new AndCondition( conditions ) );
	}
	
	public ResultSet getResults() throws SQLException
	{
		return getResults( selecting.toString(), ordering.toString(), limit, offset );
	}
	
	public ResultSet getResults( String ordering, Long limit, Long offset ) throws SQLException
	{
		return getResults( selecting.toString(), ordering, limit, offset );
	}
	
	public ResultSet getResults( String selecting, String ordering, Long limit, Long offset ) throws SQLException
	{
		StringBuilder query = new StringBuilder();
		query.append( "SELECT " );
		query.append( selecting );
		query.append( " FROM " );
		query.append( SqlUtil.namify( from ) );
		
		if (condition != null)
		{
			query.append( " WHERE ");
			condition.toQuery( query );
		}
		
		if (ordering != null && ordering.length() > 0)
		{
			query.append( " ORDER BY " ).append( ordering );
		}
		
		if (limit != null)
		{
			query.append( " LIMIT " ).append( limit );
		}
		
		if (offset != null)
		{
			query.append( " OFFSET " ).append( offset );
		}
		
		Transaction trans = Rekord.getTransaction();
		PreparedStatement stmt = trans.prepare( query.toString() );
		
		if (condition != null)
		{
			condition.toPreparedstatement( stmt, 1 );			
		}
		
		return stmt.executeQuery();
	}
	
	public HashSet<M> set() throws SQLException
	{
		return collect( new HashSet<M>(), true );
	}
	
	public ArrayList<M> list() throws SQLException
	{
		return collect( new ArrayList<M>(), true );
	}
	
	public ArrayList<M> list(boolean callPostSelect) throws SQLException
	{
		return collect( new ArrayList<M>(), callPostSelect );
	}
	
	public <C extends Collection<M>> C collect(C out, boolean callPostSelect) throws SQLException
	{
		ResultSet results = getResults( selecting.toString(), ordering.toString(), limit, offset );
		
		try 
		{
			while (results.next())
			{
				out.add( fromResultSet( results ) );
			}
		}
		finally
		{
			results.close();
		}
		
		if (callPostSelect)
		{
			postSelect( out );	
		}
		
		return out;
	}
	
	private M fromResultSet(ResultSet results) throws SQLException
	{
		final Transaction trans = Rekord.getTransaction();
		final Key key = table.keyFromResults( results );
		
		M model = trans.getCached( table, key );
		
		if (model == null)
		{
			model = table.newModel();
			
			populate( results, model );
			
			trans.cache( model );
			
			Rekord.log( Logging.CACHING, "to-cache: %s", model );
		}
		else
		{
			Rekord.log( Logging.CACHING, "from-cache: %s", model );
		}

		return model;
	}
	
	public void populate( ResultSet results, Model model ) throws SQLException
	{
		for (Field<?> f : selectFields)
		{
			model.valueOf( f ).fromSelect( results );
		}
	}
	
	public void postSelect(Collection<M> collection) throws SQLException
	{
		for (M model : collection)
		{
			postSelect( model );
		}
	}
	
	public void postSelect(M model) throws SQLException
	{
		for (Field<?> f : selectFields)
		{
			model.valueOf( f ).postSelect( model, this );
		}
	}

	public <T> T grab( String columnName ) throws SQLException
	{
		T value = null;
		
		ResultSet results = getResults( columnName, ordering.toString(), limit, offset );

		try
		{
			if (results.next())
			{
				value = (T) results.getObject( 0 );
			}
		}
		finally
		{
			results.close();
		}
		
		return value;
	}
	
	public <T> T grab(Column<T> column) throws SQLException
	{
		return grab( SqlUtil.namify( column.getName() ) );
	}
	
	public M first() throws SQLException
	{
		M model = null;
		ResultSet results = getResults( selecting.toString(), ordering.toString(), limit, offset );
		
		try 
		{
			if (results.next())
			{
				model = fromResultSet( results );
			}
		}
		finally
		{
			results.close();
		}
		
		postSelect( model );
		
		return model;
	}
	
	public int count() throws SQLException
	{
		int count = -1;
		
		ResultSet results = getResults( "COUNT(*)", null, null, null );

		try
		{
			if (results.next())
			{
				count = results.getInt( 1 );
			}
		}
		finally
		{
			results.close();
		}
		
		return count;
	}
	
	
	public boolean any() throws SQLException
	{
		ResultSet results = getResults( "1", null, 1L, null );
		
		try
		{
			return results.next();
		}
		finally
		{
			results.close();
		}
	}
	
	

}
