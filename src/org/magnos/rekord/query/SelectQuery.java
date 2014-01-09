
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.GroupExpression;


public class SelectQuery<M extends Model> extends GroupExpression<SelectQuery<M>> implements Factory<Query<M>>
{

    protected Table table;
    protected String from;
    protected StringBuilder selecting;
    protected List<Field<?>> selectFields;
    protected LoadProfile loadProfile;

    public SelectQuery( M model )
    {
        this( model.getTable() );
        this.whereKey( model.getKey() );
    }

    public SelectQuery( Table table )
    {
        super( null, null, AND );
        
        this.table = table;
        this.from = table.getQuotedName();
        this.selecting = new StringBuilder();
        this.selectFields = new ArrayList<Field<?>>();
        this.returning = this;
    }

    public SelectQuery<M> from( String from )
    {
        this.from = from;

        return this;
    }
    
    public String getFrom()
    {
        return from;
    }

    public SelectQuery<M> clear()
    {
        this.selectFields.clear();
        this.selecting.setLength( 0 );

        return this;
    }

    public SelectQuery<M> select( LoadProfile selectLoad )
    {
        Selection s = selectLoad.getSelection();

        append( selecting, ", ", s.getExpression() );

        for (Field<?> f : s.getFields())
        {
            selectFields.add( f );
        }

        loadProfile = selectLoad;

        return this;
    }

    public SelectQuery<M> select( Field<?> f, String expression )
    {
        append( selecting, ", ", expression );

        selectFields.add( f );

        return this;
    }

    public SelectQuery<M> select( Column<?> column )
    {
        return select( column, column.getQuotedName() );
    }
    
    public SelectQuery<M> select( Column<?> ... columns )
    {
        for (Column<?> c : columns)
        {
            select( c, c.getQuotedName() );
        }
        
        return this;
    }
    
    public SelectQuery<M> count()
    {
        selecting.setLength( 0 );
        selecting.append( "COUNT(*)" );
        selectFields.clear();
        
        return this;
    }
    
    public SelectQuery<M> any()
    {
        selecting.setLength( 0 );
        selecting.append( "1" );
        selectFields.clear();
        
        return this;
    }

    public LoadProfile getLoadProfile()
    {
        return loadProfile;
    }

    public SelectQuery<M> setLoadProfile( LoadProfile loadProfile )
    {
        this.loadProfile = loadProfile;
        
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
    
    public List<Field<?>> getSelectFields()
    {
        return selectFields;
    }

    private void append( StringBuilder sb, String delimiter, String text )
    {
        if (text != null && text.length() > 0)
        {
            if (sb.length() > 0)
            {
                sb.append( delimiter );
            }

            sb.append( text );
        }
    }

    public int getFieldLimit( Field<?> f )
    {
        if (loadProfile == null)
        {
            return -1;
        }

        FieldLoad fv = loadProfile.getFieldLoad( f );

        return (fv == null ? -1 : fv.getLimit());
    }

    public QueryBuilder toQueryBuilder()
    {
        QueryBuilder qb = new QueryBuilder();
        qb.append( "SELECT " );
        
        if (selecting.length() == 0)
        {
            qb.append( "*" );
        }
        else
        {
            qb.append( selecting );
        }
        
        qb.append( " FROM " );
        qb.append( from );

        if (hasConditions())
        {
            qb.append( " WHERE " );
            toQuery( qb );
        }
        
        return qb;
    }
    
    public QueryTemplate<M> newTemplate()
    {
        return toQueryBuilder().create( table, loadProfile, selectFields );
    }

    public Query<M> create()
    {
        return newTemplate().create();
    }

}
