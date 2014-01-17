
package org.magnos.rekord.query.expr;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.ColumnAlias;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.LiteralCondition;


public abstract class Expression<R, T>
{

    protected final ConditionResolver<R> resolver;
    
	public Expression(ConditionResolver<R> resolver)
	{
	    this.resolver = resolver;
	}

	protected abstract Condition newOperationCondition( Operator op, T value );

	protected abstract String getExpressionString();
	
	protected Condition newStringOperation( Operator op, String expression )
	{
		return new LiteralCondition( getExpressionString() + op.getSymbol() + expression );
	}
	
	public R eq( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.EQ, value ) );
	}
    
    public R eq( Column<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.EQ, column.getSelectionExpression() ) );
    }
    
    public R eq( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.EQ, column.getSelectionExpression() ) );
    }
	
	public R eqExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.EQ, expression ) );
	}

	public R neq( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.NEQ, value ) );
	}
	
	public R neq( Column<T> column )
	{
		return resolver.resolve( newStringOperation( Operator.NEQ, column.getSelectionExpression() ) );
	}
    
    public R neq( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.NEQ, column.getSelectionExpression() ) );
    }
	
	public R neqExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.NEQ, expression ) );
	}

	public R lt( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.LT, value ) );
	}
	
	public R lt( Column<T> column )
	{
		return resolver.resolve( newStringOperation( Operator.LT, column.getSelectionExpression() ) );
	}
    
    public R lt( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.LT, column.getSelectionExpression() ) );
    }
	
	public R ltExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.LT, expression ) );
	}

	public R gt( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.GT, value ) );
	}
	
	public R gt( Column<T> column )
	{
		return resolver.resolve( newStringOperation( Operator.GT, column.getSelectionExpression() ) );
	}
    
    public R gt( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.GT, column.getSelectionExpression() ) );
    }
	
	public R gtExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.GT, expression ) );
	}

	public R lte( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.LTEQ, value ) );
	}
	
	public R lte( Column<T> column )
	{
		return resolver.resolve( newStringOperation( Operator.LTEQ, column.getSelectionExpression() ) );
	}
    
    public R lte( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.LTEQ, column.getSelectionExpression() ) );
    }
	
	public R lteExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.LTEQ, expression ) );
	}

	public R gte( T value )
	{
		return resolver.resolve( newOperationCondition( Operator.GTEQ, value ) );
	}
	
	public R gte( Column<T> column )
	{
		return resolver.resolve( newStringOperation( Operator.GTEQ, column.getSelectionExpression() ) );
	}
    
    public R gte( ColumnAlias<T> column )
    {
        return resolver.resolve( newStringOperation( Operator.GTEQ, column.getSelectionExpression() ) );
    }
	
	public R gteExp( String expression )
	{
		return resolver.resolve( newStringOperation( Operator.GTEQ, expression ) );
	}

	public R isNull()
	{
		return resolver.resolve( new LiteralCondition( getExpressionString() + " IS NULL " ) );
	}

	public R isNotNull()
	{
		return resolver.resolve( new LiteralCondition( getExpressionString() + " IS NOT NULL " ) );
	}

	public abstract R between( T min, T max );

	public abstract R in( T... values );

	public abstract R notIn( T... values );

	public abstract R nil();

}
