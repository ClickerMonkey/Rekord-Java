package org.magnos.rekord.query.expr;

import org.magnos.rekord.Operator;
import org.magnos.rekord.query.condition.Condition;



public class ConditionExpression extends Expression<Object>
{

    public ConditionExpression( GroupExpression group, String prepend, Condition condition )
    {
        super( group, prepend );
        
        this.condition = condition;
    }

    @Override
    protected Condition newOperationCondition( Operator op, Object value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getExpressionString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupExpression between( Object min, Object max )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupExpression in( Object... values )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupExpression notIn( Object... values )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupExpression nil()
    {
        throw new UnsupportedOperationException();
    }

}
