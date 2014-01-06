
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldView;
import org.magnos.rekord.Key;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Order;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Type;
import org.magnos.rekord.View;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.OperatorCondition;
import org.magnos.rekord.query.expr.GroupExpression;


public class SelectQuery<M extends Model> extends GroupExpression
{

	protected Table table;
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
		this( model.getTable() );
		this.byKey( model.getKey() );
	}
	
	public SelectQuery( Table table )
	{
		this.table = table;
		this.from = table.getQuotedName();
		this.selecting = new StringBuilder();
		this.ordering = new StringBuilder();
		this.selectFields = new ArrayList<Field<?>>();
	}
	
	public SelectQuery<M> from( String from )
	{
		this.from = from;
		
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
	
	public GroupExpression

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
	
	public int getFieldLimit(Field<?> f)
	{
		if (view == null)
		{
			return -1;
		}
		
		FieldView fv = view.getFieldView( f );
		
		return (fv == null ? -1 : fv.getLimit());
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
		query.append( from );
		
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
    
    public void postSelect(Collection<M> collection) throws SQLException
    {
        postSelect( collection, view, selectFields );
    }
    
    public void postSelect(M model) throws SQLException
    {
        postSelect( model, view, selectFields );
    }
	
	private M fromResultSet(ResultSet results) throws SQLException
	{
	    return fromResultSet( results, table, view, selectFields );
	}
	
	public void populate( ResultSet results, Model model ) throws SQLException
	{
	    populate( results, model, view, selectFields );
	}
	
	public <I, O> O grab( String columnName, Type<I> type, Converter<I, O> converter ) throws SQLException
	{
		ResultSet results = getResults( columnName, ordering.toString(), limit, offset );
		O output = null;

		try
		{
			if (results.next())
			{
				I input = type.fromResultSet( results, 1, true ); 
			    output = converter.convertFrom( input );
			}
		}
		finally
		{
			results.close();
		}
		
		return output;
	}
	
	public <T> T grab( Column<T> column ) throws SQLException
	{
		return grab( column.getSelectionExpression(), column.getType(), column.getConverter() );
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
		
		if (model != null)
		{
		    postSelect( model );    
		}
		
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
	
	
	
	
	public static <M extends Model, C extends Collection<M>> C collect(ResultSet results, Table table, View view, List<Field<?>> fields, C out, boolean callPostSelect) throws SQLException
    {
        try 
        {
            while (results.next())
            {
                M model = fromResultSet( results, table, view, fields ); 
                
                out.add( model );
            }
        }
        finally
        {
            results.close();
        }
        
        if (callPostSelect)
        {
            postSelect( out, view, fields );
        }
        
        return out;
    }
	
	public static <M extends Model> M fromResultSet(ResultSet results, Table table, View view, List<Field<?>> fields) throws SQLException
    {
        final Transaction trans = Rekord.getTransaction();
        final Key key = table.keyFromResults( results );
        
        M model = trans.getCached( table, key );
        
        if (model == null)
        {
            model = table.newModel();
            
            populate( results, model, view, fields );
            
            if (trans.cache( model ))
            {
                Rekord.log( Logging.CACHING, "to-cache: %s", model );    
            }
        }
        else
        {
            model.load( view );
            
            Rekord.log( Logging.CACHING, "from-cache: %s", model );
        }

        return model;
    }
    
    public static void populate( ResultSet results, Model model, View view, List<Field<?>> fields) throws SQLException
    {
        if (view != null)
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).fromSelect( results, view.getFieldView( f ) );
            }    
        }
        else
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).fromSelect( results, FieldView.DEFAULT );
            }  
        }
    }
    
    public static <M extends Model> void postSelect(Collection<M> collection, View view, List<Field<?>> fields) throws SQLException
    {
        for (M model : collection)
        {
            postSelect( model, view, fields );
        }
    }
    
    public static <M extends Model> void postSelect(M model, View view, List<Field<?>> fields) throws SQLException
    {
        if (view != null)
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).postSelect( model, view.getFieldView( f ) );
            }
        }
        else
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).postSelect( model, FieldView.DEFAULT );
            }
        }
    }

}
