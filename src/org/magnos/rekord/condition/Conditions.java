package org.magnos.rekord.condition;

import org.magnos.rekord.Operator;
import org.magnos.rekord.Value;
import org.magnos.rekord.field.Column;

public class Conditions
{

	public static AndCondition and( Condition ... conditions )
	{
		return new AndCondition( conditions );
	}
	
	public static OrCondition or( Condition ... conditions )
	{
		return new OrCondition( conditions );
	}
	
	public static OperatorCondition op( Column<?> column, Operator operator, Object value )
	{
		return new OperatorCondition( column, operator, value );
	}
	
	public static LiteralCondition literal( String literal )
	{
		return new LiteralCondition( literal );
	}
	
	public static <T> BetweenCondition<T> between( Column<T> column, T min, T max )
	{
		return new BetweenCondition<T>( column, min, max );
	}
	
	public static AndCondition where( Column<?> ... columns )
	{
		Condition[] conditions = new Condition[ columns.length ];
		
		for (int i = 0; i < columns.length; i++)
		{
			conditions[i] = new OperatorCondition( columns[i], Operator.EQ, null );
		}
		
		return new AndCondition( conditions );
	}
	
	public static void whereBind( Condition condition, Column<?>[] columns, Value<?> ... values )
	{
		AndCondition and = (AndCondition)condition;
		
		for (int i = 0; i < columns.length; i++)
		{
			OperatorCondition op = (OperatorCondition)and.conditions[i];
			Column<?> c = columns[i];
			
			op.value = values[ c.getIndex() ].get( null );
		}
	}
	
}
