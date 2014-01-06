package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
	public void toQuery( StringBuilder query )
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

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < conditions.length; i++)
		{
			paramIndex = conditions[i].toPreparedstatement( stmt, paramIndex );
		}
		
		return paramIndex;
	}

}
