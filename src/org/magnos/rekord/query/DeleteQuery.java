package org.magnos.rekord.query;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.query.expr.GroupExpression;


public class DeleteQuery<M extends Model> extends GroupExpression<DeleteQuery<M>> implements Factory<Query<M>>
{

    public final Table table;
    public String from;
    
    public DeleteQuery( Table table )
    {
        this.table = table;
        this.from = table.getQuotedName();
        this.returning = this;
    }
    
    public DeleteQuery<M> from( String from )
    {
        this.from = from;
        
        return this;
    }
    
    public QueryTemplate<M> newTemplate()
    {
        QueryBuilder qb = new QueryBuilder();
        
        qb.append( "DELETE FROM " );
        qb.append( from );
        
        if (hasConditions())
        {
            qb.append( " WHERE " );
            toQuery( qb );
        }
        
        return qb.create( table );
    }
    
    @Override
    public Query<M> create()
    {
        return newTemplate().create();
    }
    
    public static QueryTemplate<Model> forTable( Table table )
    {
        DeleteQuery<Model> delete = new DeleteQuery<Model>( table );
        
        delete.whereKeyBind( table );
        
        return delete.newTemplate();
    }

}
