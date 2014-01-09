package org.magnos.rekord.query.expr;

import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.LiteralCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class GivenExpression<R> extends Expression<R, Object>
{
	
	public final String expression;
	
	public GivenExpression(ConditionResolver<R> resolver, String expression)
	{
	    super( resolver );
	    
		this.expression = expression;
	}

	@Override
	protected Condition newOperationCondition( Operator op, Object value )
	{
		return new OperatorCondition<Object>( expression, op, value );
	}

	@Override
	protected String getExpressionString()
	{
		return expression;
	}

	@Override
	public R between( Object min, Object max )
	{
		return resolver.resolve( new BetweenCondition<Object>( expression, min, max ) );
	}

	@Override
	public R in( Object ... values )
	{
		return resolver.resolve( new InCondition<Object>( expression, false, values ) );
	}

	@Override
	public R notIn( Object ... values )
	{
		return resolver.resolve( new InCondition<Object>( expression, true, values ) );
	}

	@Override
	public R nil()
	{
		return resolver.resolve( new LiteralCondition( expression ) );
	}
	
}
