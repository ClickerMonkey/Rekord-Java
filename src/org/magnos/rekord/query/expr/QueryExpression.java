package org.magnos.rekord.query.expr;

import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.QueryCondition;


public class QueryExpression<R, T> extends Expression<R, T>
{
    
    public SelectQuery<?> subquery;

    public QueryExpression( ConditionResolver<R> resolver, SelectQuery<?> subquery )
    {
        super( resolver );
        
        this.subquery = subquery;
    }

    @Override
    protected Condition newOperationCondition( Operator op, T value )
    {
        return new QueryCondition( subquery, op.getSymbol() + "?", value );
    }

    @Override
    protected String getExpressionString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Condition newStringOperation( Operator op, String expression )
    {
        return new QueryCondition( subquery, op.getSymbol() + expression );
    }
    
    @Override
    public R between( T min, T max )
    {
        return resolver.resolve( new QueryCondition( subquery, " BETWEEN ? AND ?", min, max ) );
    }

    @Override
    public R in( T... values )
    {
        String in = " IN (" + InCondition.generateParameters( values.length ) + ")";
        
        return resolver.resolve( new QueryCondition( subquery, in, values ) );
    }

    @Override
    public R notIn( T... values )
    {
        String notIn = " NOT IN (" + InCondition.generateParameters( values.length ) + ")";
        
        return resolver.resolve( new QueryCondition( subquery, notIn, values ) );
    }

    @Override
    public R nil()
    {
        return resolver.resolve( null );
    }

}
