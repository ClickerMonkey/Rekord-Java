
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Model;
import org.magnos.rekord.Order;
import org.magnos.rekord.Table;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.GroupExpression;


public class SelectQuery<M extends Model> extends GroupExpression
{

	protected Table table;
	protected String from;
	protected StringBuilder selecting;
	protected StringBuilder ordering;
	protected List<Field<?>> selectFields;
	protected LoadProfile loadProfile;
	protected Long offset;
	protected Long limit;

	public SelectQuery( M model )
	{
		this( model.getTable() );
		this.whereKey( model.getKey() );
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
	
	
	public SelectQuery<M> select( LoadProfile selectLoad )
	{
	    Selection s = selectLoad.getSelection();
	    
	    selecting.append( s.getExpression() );
	    
	    for (Field<?> f : s.getFields())
	    {
	        selectFields.add( f );    
	    }
	    
	    loadProfile = selectLoad;
	    
	    return this;
	}
	
	public SelectQuery<M> select( Field<?> f, String expression )
	{
	    if (expression != null)
	    {
	        if (selecting.length() > 0)
	        {
	            selecting.append( ", " );
	        }
	        
	        selecting.append( expression );
	    }
	    
	    selectFields.add( f );
	    
	    return this;
	}
	
	public LoadProfile getLoadProfile()
	{
		return loadProfile;
	}
	
	public void setLoadProfile( LoadProfile loadProfile )
	{
		this.loadProfile = loadProfile;
	}
	
	public int getFieldLimit(Field<?> f)
	{
		if (loadProfile == null)
		{
			return -1;
		}
		
		FieldLoad fv = loadProfile.getFieldLoad( f );
		
		return (fv == null ? -1 : fv.getLimit());
	}
	
	public QueryTemplate<M> newTemplate()
	{
	    StringBuilder query = new StringBuilder();
        query.append( "SELECT " );
        query.append( selecting );
        query.append( " FROM " );
        query.append( from );
        
        if (hasConditions())
        {
            query.append( " WHERE ");
            toQuery( query );    
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
        
        return (QueryTemplate<M>) NativeQuery.parse( table, query.toString(), loadProfile, selectFields );
	}
	
	public Query<M> newQuery()
	{
	    return newTemplate().create();
	}
	
}
