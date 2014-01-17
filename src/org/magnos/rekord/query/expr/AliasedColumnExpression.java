package org.magnos.rekord.query.expr;

import org.magnos.rekord.query.ColumnAlias;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class AliasedColumnExpression<R, T> extends Expression<R, T>
{
	
	public final ColumnAlias<T> aliased;
	
	public AliasedColumnExpression(ConditionResolver<R> resolver, ColumnAlias<T> aliased)
	{
	    super( resolver );
	    
		this.aliased = aliased;
	}

	@Override
	protected Condition newOperationCondition(Operator op, T value)
	{
	    return new OperatorCondition<T>( aliased, aliased.getColumn(), op, value );
	}

	@Override
	protected String getExpressionString()
	{
	    return aliased.getSelectionExpression();
	}
	
	protected R newBindOperation( Operator op )
	{
	    return resolver.resolve( OperatorCondition.forColumnBind( aliased, aliased.getColumn(), op ) );
	}

    public R eq()
    {
        return newBindOperation( Operator.EQ );
    }

    public R neq()
    {
        return newBindOperation( Operator.LT );
    }

    public R lt()
    {
        return newBindOperation( Operator.LT );
    }

    public R gt()
    {
        return newBindOperation( Operator.GT );
    }

    public R lte()
    {
        return newBindOperation( Operator.LTEQ );
    }

    public R gte()
    {
        return newBindOperation( Operator.GTEQ );
    }
	
	@Override
	public R between( T min, T max )
	{
	    return resolver.resolve( new BetweenCondition<T>( aliased, aliased.getColumn(), min, max  ) );
	}

	@Override
	public R in( T ... values )
	{
		return resolver.resolve( new InCondition<T>( aliased, aliased.getColumn(), false, values ) );
	}

	@Override
	public R notIn( T ... values )
	{
        return resolver.resolve( new InCondition<T>( aliased, aliased.getColumn(), true, values ) );
	}

	@Override
	public R nil()
	{
		return resolver.resolve( null );
	}
	
}
