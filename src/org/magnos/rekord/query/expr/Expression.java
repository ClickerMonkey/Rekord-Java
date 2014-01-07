
package org.magnos.rekord.query.expr;

import org.magnos.rekord.Field;
import org.magnos.rekord.Operator;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.LiteralCondition;


public abstract class Expression<T>
{

	public final GroupExpression group;
	public final String prepend;

	public Expression( GroupExpression group, String prepend )
	{
		this.group = group;
		this.prepend = prepend;
	}

	protected GroupExpression addAndGet( Condition expressionCondition )
	{
	    return group.add( prepend, expressionCondition );
	}

	protected abstract Condition newOperationCondition( Operator op, T value );

	protected abstract String getExpressionString();

	
	protected GroupExpression newStringOperation( Operator op, String expression )
	{
		return addAndGet( new LiteralCondition( getExpressionString() + op.getSymbol() + expression ) );
	}
	
	
	
	public GroupExpression eq( T value )
	{
		return addAndGet( newOperationCondition( Operator.EQ, value ) );
	}
	
	public GroupExpression eq( Field<T> field )
	{
		return newStringOperation( Operator.EQ, field.getQuotedName() );
	}
	
	public GroupExpression eqExp( String expression )
	{
		return newStringOperation( Operator.EQ, expression );
	}

	public GroupExpression neq( T value )
	{
		return addAndGet( newOperationCondition( Operator.NEQ, value ) );
	}
	
	public GroupExpression neq( Field<T> field )
	{
		return newStringOperation( Operator.NEQ, field.getQuotedName() );
	}
	
	public GroupExpression neqExp( String expression )
	{
		return newStringOperation( Operator.NEQ, expression );
	}

	public GroupExpression lt( T value )
	{
		return addAndGet( newOperationCondition( Operator.LT, value ) );
	}
	
	public GroupExpression lt( Field<T> field )
	{
		return newStringOperation( Operator.LT, field.getQuotedName() );
	}
	
	public GroupExpression ltExp( String expression )
	{
		return newStringOperation( Operator.LT, expression );
	}

	public GroupExpression gt( T value )
	{
		return addAndGet( newOperationCondition( Operator.GT, value ) );
	}
	
	public GroupExpression gt( Field<T> field )
	{
		return newStringOperation( Operator.GT, field.getQuotedName() );
	}
	
	public GroupExpression gtExp( String expression )
	{
		return newStringOperation( Operator.GT, expression );
	}

	public GroupExpression lte( T value )
	{
		return addAndGet( newOperationCondition( Operator.LTEQ, value ) );
	}
	
	public GroupExpression lte( Field<T> field )
	{
		return newStringOperation( Operator.LTEQ, field.getQuotedName() );
	}
	
	public GroupExpression lteExp( String expression )
	{
		return newStringOperation( Operator.LTEQ, expression );
	}

	public GroupExpression gte( T value )
	{
		return addAndGet( newOperationCondition( Operator.GTEQ, value ) );
	}
	
	public GroupExpression gte( Field<T> field )
	{
		return newStringOperation( Operator.GTEQ, field.getQuotedName() );
	}
	
	public GroupExpression gteExp( String expression )
	{
		return newStringOperation( Operator.GTEQ, expression );
	}

	public GroupExpression isNull()
	{
		return addAndGet( new LiteralCondition( getExpressionString() + " IS NULL " ) );
	}

	public GroupExpression isNotNull()
	{
		return addAndGet( new LiteralCondition( getExpressionString() + " IS NOT NULL " ) );
	}

	public abstract GroupExpression between( T min, T max );

	public abstract GroupExpression in( T... values );

	public abstract GroupExpression notIn( T... values );

	public abstract GroupExpression nil();

}
