package org.magnos.rekord.query.expr;

import org.magnos.rekord.Operator;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class ColumnExpression<T> extends Expression<T>
{
	
	public final Column<T> column;
	
	public ColumnExpression(GroupExpression group, String prepend, Column<T> column)
	{
		super( group, prepend );
		
		this.column = column;
	}

	@Override
	protected Condition newOperationCondition(Operator op, T value)
	{
		return new OperatorCondition<T>( column, op, value );
	}

	@Override
	protected String getExpressionString()
	{
		return column.getQuotedName();
	}

	@Override
	public GroupExpression between( T min, T max )
	{
		return setAndGet( new BetweenCondition<T>( column, min, max ) );
	}

	@Override
	public GroupExpression in( T ... values )
	{
		return setAndGet( new InCondition<T>( column, false, values ) );
	}

	@Override
	public GroupExpression notIn( T ... values )
	{
		return setAndGet( new InCondition<T>( column, true, values ) );
	}

	@Override
	public GroupExpression nil()
	{
		return group;
	}
	
}
