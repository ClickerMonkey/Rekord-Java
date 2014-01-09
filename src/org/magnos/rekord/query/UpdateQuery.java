package org.magnos.rekord.query;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ExpressionChain;

public class UpdateQuery<M extends Model> extends ExpressionChain<UpdateQuery<M>> implements Factory<Query<M>>
{
    
    public final Table table;
    public QueryBuilder set;
    
    public UpdateQuery( Table table )
    {
        this.table = table;
        this.set = new QueryBuilder();
        this.parent = this;
    }
    
    public UpdateQuery<M> reset()
    {
        set.clear();
        
        return this;
    }
    
    public UpdateQuery<M> set( Queryable queryable )
    {
        Field<?> f = queryable.getField();
        
        if (queryable.isUpdatable() && (f instanceof Column))
        {
            Column<?> c = (Column<?>)f;
            
            set.pad( ", " );
            set.append( c.getQuotedName() );
            set.append( " = " );
            set.append( c.getName(), queryable.getSaveExpression(), c, null, c.getType() );
        }
        
        return this;
    }
    
    public <T> UpdateQuery<M> set( Column<T> column )
    {
        set.pad( ", " );
        set.append( column.getQuotedName() );
        set.append( " = " );
        set.append( column.getName(), column.getIn(), column, null, column.getType() );
        
        return this;
    }
    
    public <T> UpdateQuery<M> set( Column<T> column, T value )
    {
        set.pad( ", " );
        set.append( column.getQuotedName() );
        set.append( " = " );
        set.append( column.getName(), column.getIn(), column, column.getConverter().toDatabase( value ), column.getType() );
        
        return this;
    }
    
    public <T> UpdateQuery<M> setExp( Column<T> column, String expression, Object ... values )
    {
        set.pad( ", " );
        set.append( column.getQuotedName() );
        set.append( " = " );
        set.appendValuable( expression, values );
        
        return this;
    }
    
    public QueryBuilder toQueryBuilder()
    {
        QueryBuilder qb = new QueryBuilder();
        qb.append( "UPDATE " );
        qb.append( table.getQuotedName() );
        qb.append( " SET " );
        qb.append( set );
        
        if (hasConditions())
        {
            qb.append( " WHERE " );
            toQuery( qb );
        }
        
        return qb;
    }
    
    public QueryTemplate<M> newTemplate()
    {
        return toQueryBuilder().create( table );
    }
    
    @Override
    public Query<M> create()
    {
        return newTemplate().create();
    }

	public static QueryTemplate<Model> forFields( Table table, Queryable ... queryables)
	{
	    UpdateQuery<Model> update = new UpdateQuery<Model>( table );
	    
	    for (Queryable q : queryables)
	    {
	        update.set( q );
	    }
	    
		update.whereKeyBind( table );

		return update.newTemplate();
	}
	
}
