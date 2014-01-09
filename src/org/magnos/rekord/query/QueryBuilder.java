
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Field;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Type;
import org.magnos.rekord.field.Column;


public class QueryBuilder
{

	public static final Pattern BIND_PATTERN = Pattern.compile( "(\\?[\\w_\\d]*)" );

	private StringBuilder query;
	private List<QueryBind> binds;

	public QueryBuilder()
	{
		this( new StringBuilder() );
	}

	public QueryBuilder( StringBuilder query, QueryBind... binds )
	{
		this.query = query;
		this.binds = new ArrayList<QueryBind>();

		for (QueryBind qb : binds)
		{
			this.binds.add( qb );
		}
	}
	
	public boolean hasQuery()
	{
	    return query.length() > 0;
	}
	
	public void clear()
	{
	    query.setLength( 0 );
	    binds.clear();
	}
	
	public void pad( String pad )
	{
	    if (query.length() > 0)
	    {
	        query.append( pad );
	    }
	}

	public void append( String x )
	{
		query.append( x );
	}

	public void append( StringBuilder x )
	{
		query.append( x );
	}
	
	public void append( QueryBuilder qb )
	{
	    query.append( qb.query );
	    binds.addAll( qb.binds );
	}
	
	public void append( String ... x )
	{
		for (int i = 0; i < x.length; i++)
		{
			query.append( x[i] );
		}
	}

	public void appendValuable( String text, Object... values )
	{
		Matcher m = BIND_PATTERN.matcher( text );

		int valueIndex = 0;
		int start = 0;
		
		while (m.find())
		{
			String variable = m.group();
			String name = variable.substring( 1 );
			int bindStart = query.length() + m.start();
			Object value = null;
			Type<Object> type = null;
			
			if (valueIndex < values.length)
			{
				value = values[valueIndex];
				type = Rekord.getTypeForObject( value );

				valueIndex++;
			}

			binds.add( new QueryBind( name, binds.size(), null, value, type, bindStart, bindStart + 1 ) );
			
			query.append( text.substring( start, m.start() ) );
			query.append( "?" );
			start = m.end();
		}

		if (valueIndex != values.length)
		{
			throw new RuntimeException( values.length + " values given but " + valueIndex + " expected for text " + text );
		}
		
		if (start < text.length())
		{
			query.append( text.substring( start ) );
		}
	}

	public void append( String name, String in, Column<?> column, Object value, Type<Object> type )
	{
		int start = query.length();
		int end = query.length() + in.length();

		binds.add( new QueryBind( name, binds.size(), column, value, type, start, end ) );

		query.append( in );
	}
	
	public void append( String in, Column<?> column, Object value, Type<Object> type )
	{
		append( String.valueOf( binds.size() ), in, column, value, type );
	}

	public StringBuilder getQuery()
	{
		return query;
	}

	public String getQueryString()
	{
		return query.toString();
	}

	public List<QueryBind> getBinds()
	{
		return binds;
	}

	public QueryBind[] getBindsArray()
	{
		return binds.toArray( new QueryBind[binds.size()] );
	}
	
    public <M extends Model> QueryTemplate<M> create( Table table, LoadProfile loadProfile, Field<?> ... select )
    {
        return new QueryTemplate<M>( table, getQueryString(), loadProfile, getBindsArray(), select );
    }
    
    public <M extends Model> QueryTemplate<M> create( Table table )
    {
        return create( table, null );
    }
    
    public <M extends Model> QueryTemplate<M> create( Table table, LoadProfile loadProfile, List<? extends Field<?>> selectList )
    {
        Field<?>[] select = selectList.toArray( new Field[ selectList.size() ] );
        
        return create( table, loadProfile, select );
    }

	@Override
	public String toString()
	{
		return query.toString();
	}
	
}
