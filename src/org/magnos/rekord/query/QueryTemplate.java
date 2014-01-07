
package org.magnos.rekord.query;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;
import org.magnos.rekord.util.StringRange;


public class QueryTemplate<M extends Model> implements Factory<Query<M>>
{
	
	private static final Pattern SELECTION_PATTERN = Pattern.compile( "SELECT (.*?) FROM.*", Pattern.CASE_INSENSITIVE );
	private static final Pattern LIMIT_PATTERN = Pattern.compile( "LIMIT (\\d+)", Pattern.CASE_INSENSITIVE );
	private static final Pattern OFFSET_PATTERN = Pattern.compile( "OFFSET (\\d+)", Pattern.CASE_INSENSITIVE );
	
    protected final Table table;
    protected final String query;
    protected final View view;
    
    protected final QueryBind[] binds;
    protected final Map<String, QueryBind> bindMap;
    
    protected final Field<?>[] select;
    
    protected final StringRange selectRange;
    protected final StringRange limitRange;
    protected final StringRange offsetRange;
    
    public QueryTemplate( Table table, String query, View view, QueryBind[] binds, Field<?>[] select )
    {
        this.table = table;
        this.query = query;
        this.view = view;
        this.binds = binds;
        this.bindMap = new HashMap<String, QueryBind>();
        this.select = select;
        this.selectRange = getPatternRange( SELECTION_PATTERN, query );
        this.limitRange = getPatternRange( LIMIT_PATTERN, query );
        this.offsetRange = getPatternRange( OFFSET_PATTERN, query );
        
        for (QueryBind bind : binds)
        {
        	if (bind.name != null)
        	{
        		bindMap.put( bind.name, bind );
        	}
        }
    }

    @Override
    public Query<M> create()
    {
        return new Query<M>( this );
    }
    
    public Table getTable()
    {
        return table;
    }

    public String getQuery()
    {
        return query;
    }
    
    public String getQuery(String alternativeSelection)
    {
    	return selectRange.replace( query, alternativeSelection );
    }
    
    public String getQueryPage(int offset, int limit)
    {
        String alternativeQuery = query;
        
        if (offsetRange.exists())
        {
            alternativeQuery = offsetRange.replace( alternativeQuery, String.valueOf( offset ) );
        }
        else
        {
            alternativeQuery += " OFFSET " + offset;
        }
        
        if (limitRange.exists())
        {
            alternativeQuery = limitRange.replace( alternativeQuery, String.valueOf( limit ) );
        }
        else
        {
            alternativeQuery += " LIMIT " + limit;
        }
        
        return alternativeQuery;
    }

	public View getView()
    {
        return view;
    }
	
	public boolean hasView()
	{
		return (view != null);
	}

    public Field<?>[] getSelection()
    {
        return select;
    }
    
    public QueryBind[] getBinds()
    {
    	return binds;
    }
    
    public int indexOf( String name )
    {
    	QueryBind bind = bindMap.get( name );
    	
    	return bind == null ? -1 : bind.index;
    }
    
    public QueryBind getBind( int index )
    {
    	return binds[ index ];
    }
    
    public int getBindCount()
    {
    	return binds.length;
    }
    
    private static StringRange getPatternRange( Pattern pattern, String text )
    {
        StringRange range = new StringRange();
        
        Matcher matcher = pattern.matcher( text );
        
        if (matcher.matches())
        {
            range.start = matcher.start( 1 );
            range.end = matcher.end( 1 );
        }
        
        return range;
    }

}
