package org.magnos.rekord.query.expr;

import org.magnos.rekord.Operator;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.LiteralCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class StringExpression extends Expression<Object>
{
	
	public final String expression;
	
	public StringExpression(GroupExpression group, String prepend, String expression)
	{
		super( group, prepend );
		
		this.expression = expression;
	}
	
	public StringExpression(GroupExpression group, String prepend, String expression, Condition condition)
	{
		super( group, prepend );
		
		this.expression = expression;
		this.condition = condition;
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
	public GroupExpression between( Object min, Object max )
	{
		return setAndGet( new BetweenCondition<Object>( expression, min, max ) );
	}

	@Override
	public GroupExpression in( Object ... values )
	{
		return setAndGet( new InCondition<Object>( expression, false, values ) );
	}

	@Override
	public GroupExpression notIn( Object ... values )
	{
		return setAndGet( new InCondition<Object>( expression, true, values ) );
	}

	@Override
	public GroupExpression nil()
	{
		return setAndGet( new LiteralCondition( expression ) );
	}
	
}
