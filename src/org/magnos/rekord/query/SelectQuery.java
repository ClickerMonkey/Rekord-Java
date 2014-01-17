
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ExpressionChain;


public class SelectQuery<M extends Model> extends ExpressionChain<SelectQuery<M>> implements Factory<Query<M>>
{

    protected Table table;
    protected TableAlias tableAlias;
    protected StringBuilder selecting;
    protected List<Field<?>> selectFields;
    protected LoadProfile loadProfile;
    protected int[] tableAliasCounts = {};
    protected List<Join> joins;

    public SelectQuery( M model )
    {
        this( model.getTable() );

        this.whereKey( model.getKey() );
    }

    public SelectQuery( Table table )
    {
        this.table = table;
        this.tableAlias = alias( table );
        this.columnResolver = tableAlias;
        this.selecting = new StringBuilder();
        this.selectFields = new ArrayList<Field<?>>();
        this.joins = new ArrayList<Join>();
        this.parent = this;
    }

    public TableAlias alias( Table table )
    {
        int i = table.getIndex();

        if (tableAliasCounts.length <= i)
        {
            tableAliasCounts = Arrays.copyOf( tableAliasCounts, i + 1 );
        }

        TableAlias alias = new TableAlias( table, table.getAlias() + tableAliasCounts[i] );

        tableAliasCounts[i]++;

        return alias;
    }

    private Join join( Join join )
    {
        joins.add( join );

        return join;
    }

    public Join join( String joinType, Table table )
    {
        return join( new Join( joinType, table ) );
    }

    public Join join( String joinType, TableAlias tableAlias )
    {
        return join( new Join( joinType, tableAlias ) );
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
        Field<?>[] fields = s.getFields();
        
        for(Field<?> f : fields)
        {
            String se = f.getSelectExpression( columnResolver, selectLoad.getFieldLoad( f ) );
            
            if (se != null)
            {
                append( selecting, ", ", se );
            }
        }
        
        for (Field<?> f : fields)
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

    public SelectQuery<M> select( Field<?> f, String expression, String alias )
    {
        return select( f, expression + " AS " + alias );
    }

    public SelectQuery<M> select( Column<?> column )
    {
        return select( column, columnResolver.resolve( column ) );
    }

    public SelectQuery<M> select( Column<?>... columns )
    {
        for (Column<?> c : columns)
        {
            select( c, columnResolver.resolve( c ) );
        }

        return this;
    }

    public SelectQuery<M> select( ColumnAlias<?> columnAlias )
    {
        return select( columnAlias.getColumn(), columnAlias.getSelectionExpression() );
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

    public Table getTable()
    {
        return table;
    }
    
    public TableAlias getTableAlias()
    {
        return tableAlias;
    }
    
    public void toQueryBuilder(QueryBuilder qb)
    {
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
        qb.append( tableAlias.getSelectionExpression() );

        for (Join j : joins)
        {
            j.toQuery( qb );
        }
        
        if (hasConditions())
        {
            qb.append( " WHERE " );
            toQuery( qb );
        }
    }
    
    public QueryBuilder toQueryBuilder()
    {
        QueryBuilder qb = new QueryBuilder();
        
        toQueryBuilder( qb );

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
