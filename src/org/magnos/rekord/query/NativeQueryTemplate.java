
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;
import org.magnos.rekord.field.Column;


public class NativeQueryTemplate<M extends Model> implements Factory<NativeQuery<M>>
{

    public static final Pattern TOKEN_PATTERN = Pattern.compile( "(#[\\w_][\\w_\\d\\$]*|\\?[\\w_][\\w_\\d\\$]*)" );

    public class NativeQuerySet
    {
        public final String name;
        public final int index;
        
        public NativeQuerySet(String name, int index)
        {
            this.name = name;
            this.index = index;
        }
    }
    
    protected final String name;
    protected final Table table;
    protected final String nativeQuery;
    protected final String query;
    protected final View view;
    
    protected final List<NativeQuerySet> sets;
    protected final Map<String, NativeQuerySet> setMap;
    
    protected final List<Field<?>> selectFields;

    public NativeQueryTemplate( String name, Table table, String nativeQuery, View view )
    {
        this.name = name;
        this.table = table;
        this.nativeQuery = nativeQuery;
        this.view = view;
        this.sets = new ArrayList<NativeQuerySet>();
        this.setMap = new HashMap<String, NativeQuerySet>();
        this.selectFields = new ArrayList<Field<?>>();

        StringBuilder queryBuilder = new StringBuilder();
        Matcher matcher = TOKEN_PATTERN.matcher( nativeQuery );

        int start = 0;

        while (matcher.find())
        {
            String token = matcher.group();
            char indicator = token.charAt( 0 );
            String tokenName = token.substring( 1 );
            String tokenReplacement = null;
            
            switch (indicator)
            {
            case '?':
                NativeQuerySet set = new NativeQuerySet( tokenName, sets.size() );
                sets.add( set );
                setMap.put( tokenName, set );
                tokenReplacement = "?";
                break;
            case '#':
                Column<?> column = table.getField( tokenName );
                if (column == null)
                {
                    throw new RuntimeException( "Error parsing NativeQuery, column " + tokenName + " does not exist on table " + table.getName() );
                }
                selectFields.add( column );
                tokenReplacement = column.getQuotedName();
                break;
            }

            queryBuilder.append( nativeQuery.substring( start, matcher.start() ) );
            queryBuilder.append( tokenReplacement );
            start = matcher.end();
        }

        if (start < nativeQuery.length())
        {
            queryBuilder.append( nativeQuery.substring( start ) );
        }

        this.query = queryBuilder.toString();
    }

    @Override
    public NativeQuery<M> create()
    {
        return new NativeQuery<M>( this );
    }
    
    public String getName()
    {
        return name;
    }

    public Table getTable()
    {
        return table;
    }

    public String getNativeQuery()
    {
        return nativeQuery;
    }

    public String getQuery()
    {
        return query;
    }

    public View getView()
    {
        return view;
    }

    public List<Field<?>> getSelectFields()
    {
        return selectFields;
    }
    
    public int setIndex( String name )
    {
        return setMap.get( name ).index;
    }
    
    public NativeQuerySet setAt( int index ) 
    {
        return sets.get( index );
    }
    
    public int sets()
    {
        return sets.size();
    }
    
}
