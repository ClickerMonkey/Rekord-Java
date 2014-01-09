package org.magnos.rekord.query.expr;

import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.LiteralCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class GivenExpression<R> extends Expression<R, Object>
{
	
	public final String expression;
	
	public GivenExpression(R returning, GroupExpression<R> group, String prepend, String expression)
	{
		super( returning, group, prepend );
		
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
		return addAndGet( new BetweenCondition<Object>( expression, min, max ) );
	}

	@Override
	public R in( Object ... values )
	{
		return addAndGet( new InCondition<Object>( expression, false, values ) );
	}

	@Override
	public R notIn( Object ... values )
	{
		return addAndGet( new InCondition<Object>( expression, true, values ) );
	}

	@Override
	public R nil()
	{
		return addAndGet( new LiteralCondition( expression ) );
	}
	
}
