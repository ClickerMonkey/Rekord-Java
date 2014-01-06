
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


public class QueryTemplate<M extends Model> implements Factory<Query<M>>
{
	
	private static final Pattern SELECTION_PATTERN = Pattern.compile( "SELECT (.*?) FROM.*", Pattern.CASE_INSENSITIVE );

    protected final Table table;
    protected final String query;
    protected final View view;
    
    protected final QueryBind[] binds;
    protected final Map<String, QueryBind> bindMap;
    
    protected final Field<?>[] select;
    
    protected final int selectStart;
    protected final int selectEnd;
    
    public QueryTemplate( Table table, String query, View view, QueryBind[] binds, Field<?>[] select )
    {
        this.table = table;
        this.query = query;
        this.view = view;
        this.binds = binds;
        this.bindMap = new HashMap<String, QueryBind>();
        this.select = select;
        
        for (QueryBind bind : binds)
        {
        	if (bind.name != null)
        	{
        		bindMap.put( bind.name, bind );
        	}
        }
        
        Matcher matcher = SELECTION_PATTERN.matcher( query );
        
        if (matcher.matches()) {
       		this.selectStart = matcher.start( 1 );
       		this.selectEnd = matcher.end( 1 );
        } else {
        	this.selectStart = -1;
        	this.selectEnd = -1;
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
    	String alternativeQuery = query;
    	
    	if (selectStart != -1 && selectEnd != -1)
    	{
    		alternativeQuery = query.substring( 0, selectStart ) + alternativeSelection + query.substring( selectEnd );
    	}
    	
    	return alternativeQuery;
    }

    public int getSelectStart()
	{
		return selectStart;
	}

	public int getSelectEnd()
	{
		return selectEnd;
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
    
}
