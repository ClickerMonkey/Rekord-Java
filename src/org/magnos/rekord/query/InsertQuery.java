
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ExpressionChain;


public class InsertQuery<M extends Model> extends ExpressionChain<InsertQuery<M>> implements Factory<Query<M>>
{

    public final Table table;
    public String into;
    public QueryBuilder columns;
    public QueryBuilder values;
    public QueryBuilder returning;
    public List<Field<?>> returningFields;

    public InsertQuery( Table table )
    {
        this.table = table;
        this.into = table.getQuotedName();
        this.columns = new QueryBuilder();
        this.values = new QueryBuilder();
        this.returning = new QueryBuilder();
        this.returningFields = new ArrayList<Field<?>>();
    }

    public InsertQuery<M> into( String into )
    {
        this.into = into;

        return this;
    }

    public InsertQuery<M> reset()
    {
        columns.clear();
        values.clear();
        returning.clear();
        returningFields.clear();

        return this;
    }

    public InsertQuery<M> insert( Queryable queryable )
    {
        Field<?> f = queryable.getField();
        InsertAction action = queryable.getInsertAction();

        if (action == InsertAction.RETURN)
        {
            returning( f );
        }
        else
        {
            if (f instanceof Column && action == InsertAction.VALUE)
            {
                insert( (Column<?>)f );    
            }
            if (f.is( Field.RETURN_ON_SAVE ))
            {
                returning( f );
            }
        }

        return this;
    }

    public <T> InsertQuery<M> insert( Column<T> column )
    {
        columns.pad( ", " );
        columns.append( column.getQuotedName() );

        values.pad( ", " );
        
        String save = column.getSaveExpression();
        
        if (save.indexOf( '?' ) == -1)
        {
            values.append( save );
        }
        else
        {
            values.append( column.getName(), save, column, null, column.getType() );    
        }

        return this;
    }

    public <T> InsertQuery<M> insert( Column<T> column, T value )
    {
        columns.pad( ", " );
        columns.append( column.getQuotedName() );

        values.pad( ", " );
        values.append( column.getName(), column.getOut(), column, column.getConverter().toDatabase( value ), column.getType() );
        
        return this;
    }

    public <T> InsertQuery<M> returning( Field<T> field )
    {
        if (field instanceof Column)
        {
            returning.pad( ", " );
            returning.append( field.getQuotedName() );
        }

        returningFields.add( field );

        return this;
    }

    public QueryTemplate<M> newTemplateFromSelect( SelectQuery<M> select )
    {
        QueryBuilder columns = new QueryBuilder();
        
        for (Field<?> f : select.getSelectFields() )
        {
            if (f instanceof Column)
            {
                columns.pad( ", " );
                columns.append( f.getQuotedName() );
            }
        }
        
        QueryBuilder qb = new QueryBuilder();
        qb.append( "INSERT INTO " );
        qb.append( into );
        qb.append( " (" );
        qb.append( columns );
        qb.append( ") " );
        qb.append( select.toQueryBuilder() );
        
        return qb.create( table );
    }
    
    public QueryBuilder toQueryBuilder()
    {
        QueryBuilder qb = new QueryBuilder();

        qb.append( "INSERT INTO " );
        qb.append( into );
        qb.append( " " );

        if (columns.hasQuery())
        {
            qb.append( "(" );
            qb.append( columns );
            qb.append( ") VALUES (" );
            qb.append( values );
            qb.append( ")" );
        }
        else
        {
            qb.append( "DEFAULT VALUES" );
        }

        if (returning.hasQuery())
        {
            qb.append( " RETURNING " );
            qb.append( returning );
        }
        
        return qb;
    }

    public QueryTemplate<M> newTemplate()
    {
        return toQueryBuilder().create( table, null, returningFields );
    }

    @Override
    public Query<M> create()
    {
        return newTemplate().create();
    }

    public static QueryTemplate<Model> forFields( Table table, Queryable... queryables )
    {
        InsertQuery<Model> insert = new InsertQuery<Model>( table );

        Set<Queryable> queryableSet = new HashSet<Queryable>();

        for (Queryable q : queryables)
        {
            queryableSet.add( q );

            insert.insert( q );
        }

        for (Field<?> f : table.getGivenFields())
        {
            if (f.is( Field.HAS_DEFAULT ) && !queryableSet.contains( f ))
            {
                insert.returning( f );
            }
        }

        return insert.newTemplate();
    }

}
