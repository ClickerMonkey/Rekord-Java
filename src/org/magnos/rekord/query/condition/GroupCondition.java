package org.magnos.rekord.query.condition;

import org.magnos.rekord.query.QueryBuilder;

public class GroupCondition implements Condition
{
	
	public final String delimiter;
	public final Condition[] conditions;
	
	public GroupCondition(String delimiter, Condition ... conditions) 
	{
		this.delimiter = delimiter;
		this.conditions = conditions;
	}

	@Override
	public void toQuery( QueryBuilder query )
	{
		query.append( "(" );
		
		for (int i = 0; i < conditions.length; i++)
		{
			if (i > 0)
			{
				query.append( delimiter );
			}
			
			conditions[i].toQuery( query );
		}
		
		query.append( ")" );
	}

}
