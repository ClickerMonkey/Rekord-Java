package org.magnos.rekord.query.condition;

import org.magnos.rekord.query.QueryBuilder;

public class CustomCondition implements Condition
{

	public String expression;
	public Object[] values;
	
	public CustomCondition(String expression, Object ... values)
	{
		this.expression = expression;
		this.values = values;
	}
	
	@Override
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( expression, values );
	}

}
