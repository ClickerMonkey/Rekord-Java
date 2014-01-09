package org.magnos.rekord.query.expr;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.BetweenCondition;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.InCondition;
import org.magnos.rekord.query.condition.OperatorCondition;

public class ColumnExpression<R, T> extends Expression<R, T>
{
	
	public final Column<T> column;
	
	public ColumnExpression(R returning, GroupExpression<R> group, String prepend, Column<T> column)
	{
		super( returning, group, prepend );
		
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
	public R between( T min, T max )
	{
		return addAndGet( new BetweenCondition<T>( column, min, max ) );
	}

	@Override
	public R in( T ... values )
	{
		return addAndGet( new InCondition<T>( column, false, values ) );
	}

	@Override
	public R notIn( T ... values )
	{
		return addAndGet( new InCondition<T>( column, true, values ) );
	}

	@Override
	public R nil()
	{
		return returning;
	}
	
}
