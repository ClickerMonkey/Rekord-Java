package org.magnos.rekord.query.condition;

import org.magnos.rekord.query.QueryBuilder;

public class PrependedCondition implements Condition
{
	
	public final String prepend;
	public final Condition condition;
	
	public PrependedCondition(String prepend, Condition condition)
	{
		this.prepend = prepend;
		this.condition = condition;
	}

	@Override
	public void toQuery( QueryBuilder query )
	{
		condition.toQuery( query );
	}

}
