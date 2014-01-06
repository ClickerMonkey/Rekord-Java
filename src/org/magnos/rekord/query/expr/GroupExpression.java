package org.magnos.rekord.query.expr;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Field;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.CustomCondition;

public class GroupExpression implements Condition
{
	public static final String AND 		= " AND ";
	public static final String AND_NOT 	= " AND NOT ";
	public static final String OR 		= " OR ";
	public static final String OR_NOT	= " OR NOT ";
	
	public final GroupExpression parent;
	public final String prepend;
	public final List<Object> children;
	
	public GroupExpression()
	{
		this( null, AND );
	}
	
	public GroupExpression( GroupExpression parent, String prepend )
	{
		this.parent = parent;
		this.prepend = prepend;
		this.children = new ArrayList<Object>();
	}
	
	public void toQuery(StringBuilder query)
	{
		int index = 0;
		
		for (Object o : children)
		{
			if (o instanceof GroupExpression)
			{
				GroupExpression ge = (GroupExpression)o;
				
				if (index > 0)
				{
					query.append( ge.prepend );
				}
				
				query.append( "(" );
				ge.toQuery( query );
				query.append( ")" );
			}
			else if (o instanceof Expression)
			{
				Expression<?> e = (Expression<?>)o;
				
				if (index > 0)
				{
					query.append( e.prepend );
				}
				
				e.condition.toQuery( query );
			}
			
			index++;
		}
	}
	
	public int toPreparedstatement(PreparedStatement stmt, int paramIndex) throws SQLException
	{
		for (Object o : children)
		{
			if (o instanceof GroupExpression)
			{
				GroupExpression ge = (GroupExpression)o;
				
				paramIndex = ge.toPreparedstatement( stmt, paramIndex );
			}
			else if (o instanceof Expression)
			{
				Expression<?> e = (Expression<?>)o;
				
				paramIndex = e.condition.toPreparedstatement( stmt, paramIndex );
			}
		}
		
		return paramIndex;
	}
	
	protected GroupExpression add(Expression<Object> fieldExpression)
	{
		children.add( fieldExpression );

		return this;
	}
	
	protected GroupExpression newChild( String prepend )
	{
		GroupExpression child = new GroupExpression( this, prepend );
		
		children.add( child );
		
		return child;
	}
	
	protected Expression<Object> newStringExpression(String prepend, String expression)
	{
		return new StringExpression( this, prepend, expression );
	}
	
	@SuppressWarnings ({ "cast", "rawtypes" } )
	protected <T> Expression<T> newFieldExpression(String prepend, Field<T> field)
	{
		if (field instanceof Column) {
			return new ColumnExpression<T>( this, prepend, (Column<T>)field );	
		}
		else if (field.is( Field.MODEL )) {
			return new ModelExpression( this, prepend, (Column<T>)field );
		}
		else if (field.is (Field.MODEL_LIST)) {
			throw new UnsupportedOperationException();
		}
		
		throw new UnsupportedOperationException();
	}
	
	protected GroupExpression newStringExpressionCustom(String prepend, String expression, Object[] values)
	{
		return add( new StringExpression( this, prepend, expression, new CustomCondition( expression, values ) ) );
	}
	
	
	public GroupExpression end()
	{
		return parent;
	}
	
	
	public Expression<Object> where(String expression)
	{
		return newStringExpression( AND, expression );
	}
	
	public <T> Expression<T> where(Field<T> field)
	{
		return newFieldExpression( AND, field );
	}
	
	public GroupExpression where(String expression, Object ... values)
	{
		return newStringExpressionCustom( AND, expression, values );
	}
	
	
	public GroupExpression and()
	{
		return newChild( AND );
	}
	
	public Expression<Object> and(String expression)
	{
		return newStringExpression( AND, expression );
	}
	
	public <T> Expression<T> and(Field<T> field)
	{
		return newFieldExpression( AND, field );
	}
	
	public GroupExpression and(String expression, Object ... values)
	{
		return newStringExpressionCustom( AND, expression, values );
	}
	
	
	public GroupExpression andNot()
	{
		return newChild( AND_NOT );
	}
	
	public Expression<Object> andNot(String expression)
	{
		return newStringExpression( AND_NOT, expression );
	}
	
	public <T> Expression<T> andNot(Field<T> field)
	{
		return newFieldExpression( AND_NOT, field );
	}
	
	public GroupExpression andNot(String expression, Object ... values)
	{
		return newStringExpressionCustom( AND_NOT, expression, values );
	}

	
	public GroupExpression or()
	{
		return newChild( OR );
	}
	
	public Expression<Object> or(String expression)
	{
		return newStringExpression( OR, expression );
	}
	
	public <T> Expression<T> or(Field<T> field)
	{
		return newFieldExpression( OR, field );
	}
	
	public GroupExpression or(String expression, Object ... values)
	{
		return newStringExpressionCustom( OR, expression, values );
	}

	
	public GroupExpression orNot()
	{
		return newChild( OR_NOT );
	}
	
	public Expression<Object> orNot(String expression)
	{
		return newStringExpression( OR_NOT, expression );
	}
	
	public <T> Expression<T> orNot(Field<T> field)
	{
		return newFieldExpression( OR_NOT, field );
	}
	
	public GroupExpression orNot(String expression, Object ... values)
	{
		return newStringExpressionCustom( OR_NOT, expression, values );
	}
	
}

