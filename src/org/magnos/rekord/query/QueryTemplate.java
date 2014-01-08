
package org.magnos.rekord.query;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.util.StringRange;


public class QueryTemplate<M extends Model> implements Factory<Query<M>>
{
	
	private static final Pattern SELECTION_PATTERN = Pattern.compile( "SELECT (.*?) FROM.*", Pattern.CASE_INSENSITIVE );
	
    protected final Table table;
    protected final String query;
    protected final LoadProfile loadProfile;
    
    protected final QueryBind[] binds;
    protected final Map<String, QueryBind> bindMap;
    
    protected final Field<?>[] select;
    
    protected final StringRange selectRange;
    
    public QueryTemplate( Table table, String query, LoadProfile loadProfile, QueryBind[] binds, Field<?>[] select )
    {
        this.table = table;
        this.query = query;
        this.loadProfile = loadProfile;
        this.binds = binds;
        this.bindMap = new HashMap<String, QueryBind>();
        this.select = select;
        this.selectRange = getPatternRange( SELECTION_PATTERN, query );
        
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

	public LoadProfile getLoadProfile()
    {
        return loadProfile;
    }
	
	public boolean hasLoadProfile()
	{
		return (loadProfile != null);
	}

    public Field<?>[] getSelectFields()
    {
        return select;
    }
    
    public String getSelectExpression()
    {
        return selectRange.grab( query );
    }
    
    public boolean isSelect()
    {
        return selectRange.exists();
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
