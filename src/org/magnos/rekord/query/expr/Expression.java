
package org.magnos.rekord.query.expr;

import org.magnos.rekord.Field;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.LiteralCondition;


public abstract class Expression<R, T>
{

	public final R returning;
	public final GroupExpression<R> group;
	public final String prepend;

	public Expression( R returning, GroupExpression<R> group, String prepend )
	{
	    this.returning = returning;
		this.group = group;
		this.prepend = prepend;
	}

	protected R addAndGet( Condition expressionCondition )
	{
	    return group.add( prepend, expressionCondition );
	}

	protected abstract Condition newOperationCondition( Operator op, T value );

	protected abstract String getExpressionString();

	
	protected R newStringOperation( Operator op, String expression )
	{
		return addAndGet( new LiteralCondition( getExpressionString() + op.getSymbol() + expression ) );
	}
	
	
	
	public R eq( T value )
	{
		return addAndGet( newOperationCondition( Operator.EQ, value ) );
	}
	
	public R eq( Field<T> field )
	{
		return newStringOperation( Operator.EQ, field.getQuotedName() );
	}
	
	public R eqExp( String expression )
	{
		return newStringOperation( Operator.EQ, expression );
	}

	public R neq( T value )
	{
		return addAndGet( newOperationCondition( Operator.NEQ, value ) );
	}
	
	public R neq( Field<T> field )
	{
		return newStringOperation( Operator.NEQ, field.getQuotedName() );
	}
	
	public R neqExp( String expression )
	{
		return newStringOperation( Operator.NEQ, expression );
	}

	public R lt( T value )
	{
		return addAndGet( newOperationCondition( Operator.LT, value ) );
	}
	
	public R lt( Field<T> field )
	{
		return newStringOperation( Operator.LT, field.getQuotedName() );
	}
	
	public R ltExp( String expression )
	{
		return newStringOperation( Operator.LT, expression );
	}

	public R gt( T value )
	{
		return addAndGet( newOperationCondition( Operator.GT, value ) );
	}
	
	public R gt( Field<T> field )
	{
		return newStringOperation( Operator.GT, field.getQuotedName() );
	}
	
	public R gtExp( String expression )
	{
		return newStringOperation( Operator.GT, expression );
	}

	public R lte( T value )
	{
		return addAndGet( newOperationCondition( Operator.LTEQ, value ) );
	}
	
	public R lte( Field<T> field )
	{
		return newStringOperation( Operator.LTEQ, field.getQuotedName() );
	}
	
	public R lteExp( String expression )
	{
		return newStringOperation( Operator.LTEQ, expression );
	}

	public R gte( T value )
	{
		return addAndGet( newOperationCondition( Operator.GTEQ, value ) );
	}
	
	public R gte( Field<T> field )
	{
		return newStringOperation( Operator.GTEQ, field.getQuotedName() );
	}
	
	public R gteExp( String expression )
	{
		return newStringOperation( Operator.GTEQ, expression );
	}

	public R isNull()
	{
		return addAndGet( new LiteralCondition( getExpressionString() + " IS NULL " ) );
	}

	public R isNotNull()
	{
		return addAndGet( new LiteralCondition( getExpressionString() + " IS NOT NULL " ) );
	}

	public abstract R between( T min, T max );

	public abstract R in( T... values );

	public abstract R notIn( T... values );

	public abstract R nil();

}
